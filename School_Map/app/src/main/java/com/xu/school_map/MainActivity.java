package com.xu.school_map;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.ColorStateList;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Window;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ZoomControls;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.BaiduMapOptions;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.UiSettings;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;
import com.baidu.mapapi.search.sug.OnGetSuggestionResultListener;
import com.baidu.mapapi.search.sug.SuggestionResult;
import com.baidu.mapapi.search.sug.SuggestionSearch;
import com.baidu.mapapi.search.sug.SuggestionSearchOption;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private MapView mMapView = null;
    private BaiduMap mBaiduMap;
    //定位的客户端
    private LocationClient mLocationClient;
    private boolean isFristLocation = true;
    private UiSettings mUiSettings;
    //定位的监听器
    public MyBDAbstractLocationListener myMyBDAbstractLocationListener;
    //当前定位的模式
    private MyLocationConfiguration.LocationMode mCurrentMode = MyLocationConfiguration.LocationMode.NORMAL;;
    private int GPS_REQUEST_CODE = 10;
    // 最新一次的经纬度
    private double mCurrentLantitude;
    private double mCurrentLongitude;
    //方向传感器的监听器
    private MyOrientationListener myOrientationListener;
    //方向传感器X方向的值
    private int mXDirection;
    //当前的精度
    private float mCurrentAccracy;
    //搜索关键字输入窗口
    private AutoCompleteTextView keyWordsView = null;
    private myAdapter<String> sugAdapter = null;
    private PlaceInfo placeInfo=new PlaceInfo();
    //地球半径
    private final double EARTH_RADIUS = 6378137;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        SDKInitializer.initialize(getApplicationContext());

        setContentView(R.layout.activity_main);
        //定义AutoCompleteTextView控件
        keyWordsView = (AutoCompleteTextView) findViewById(R.id.inputBox);
        //定义匹配源的adapter
        sugAdapter = new myAdapter<String>(this, android.R.layout.simple_dropdown_item_1line,placeInfo.placeName);
        //设置 匹配源的adapter 到 AutoCompleteTextView控件
        keyWordsView.setAdapter(sugAdapter);
        keyWordsView.setThreshold(1);
        // 表示当AutoCompleteTextView控件中某一项被点击的监听事件
        keyWordsView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                // 根据下标获取点击item的信息
                String str = adapterView.getItemAtPosition(i).toString();
                int index=placeInfo.placeMap.get(str);
                LatLng poi=new LatLng(placeInfo.latitude[index],placeInfo.longitude[index]);
                createPoi(poi,placeInfo.placeName.get((index)));
                mBaiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(poi));
            }
        });

        mMapView = (MapView) findViewById(R.id.bmapView);
        mBaiduMap=mMapView.getMap();
        //设置地图单击事件监听
        mBaiduMap.setOnMapClickListener(new BaiduMap.OnMapClickListener() {
            //地图单击事件回调函数
            @Override
            public void onMapClick(LatLng point) {
                List<Integer> poi=new ArrayList<Integer>();
                for(int i=0;i<placeInfo.latitude.length;i++) {
                    if(GetDistance(point.latitude,point.longitude,placeInfo.latitude[i],placeInfo.longitude[i])<100) {
                        poi.add(i);
                    }
                }
                mBaiduMap.clear();
                if(!poi.isEmpty()) {
                    createPois(poi);
                }
            }
            //地图内 Poi 单击事件回调函数
            //MapPoi:点击地图 Poi 点时，该兴趣点的描述信息
            @Override
            public boolean onMapPoiClick(MapPoi poi) {
                return false;
            }
        });
        //BaiduMapOptions options = new BaiduMapOptions();

        //LatLng GEO_QDU = new LatLng(36.077, 120.424);

        MapStatus.Builder builder = new MapStatus.Builder();
        builder.zoom(19.0f);
        //builder.target(GEO_QDU);
        //缩放级别
        //青岛大学为地图中心，logo在左上角
        //mMapView.getMap().setMapStatus(MapStatusUpdateFactory.newLatLng(GEO_QDU));
        mBaiduMap.setMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
        //map1.getMapView().setLogoPosition(LogoPosition.logoPostionleftTop);

        //mMapView.getMap().setMyLocationEnabled(true);
        //定位
        mBaiduMap.setMyLocationEnabled(true);
        // 隐藏百度的LOGO
        View child = mMapView.getChildAt(1);
        if (child != null && (child instanceof ImageView || child instanceof ZoomControls)) {
            child.setVisibility(View.INVISIBLE);
        }

        // 初始化定位
        useGPS();
        // 初始化传感器
        initOritationListener();
        //实例化UiSettings类对象
        //mUiSettings = mBaiduMap.getUiSettings();
        //默认显示地图标注
        //mBaiduMap.showMapPoi(false);
        //createPoi();

    }

    @Override
    protected void onStart() { // 开启图层定位
        mBaiduMap.setMyLocationEnabled(true);
        if (!mLocationClient.isStarted()) {
            mLocationClient.start();
        } // 开启方向传感器
        myOrientationListener.start(); super.onStart();
    }

    @Override protected void onStop() { // 关闭图层定位
        mBaiduMap.setMyLocationEnabled(false); mLocationClient.stop(); // 关闭方向传感器
        myOrientationListener.stop();
        super.onStop();
    }

    @Override
    protected void onResume() {
        mMapView.onResume();
        super.onResume();

    }

    @Override
    protected void onPause() {
        mMapView.onPause();
        super.onPause();

    }

    @Override
    protected void onDestroy() {
        mLocationClient.stop();
        mBaiduMap.setMyLocationEnabled(false);
        mMapView.onDestroy();
        MapView.setMapCustomEnable(false);
        mMapView = null;
        super.onDestroy();

    }

    //按钮的单击事件
    public void onClick(View view) {
        switch (view.getId()) {
            //切换地图类型
            case R.id.SatelliteBtn:

                mBaiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE); //切换地图类型
                break;
            case R.id.GeneralBtn:
                mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL); //切换地图类型
                break;
            //定位当前位置
            case R.id.GpsButton:
                openGPSSettings();
                break;
        }
    }


    private class MyBDAbstractLocationListener extends BDAbstractLocationListener {
        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            if (bdLocation == null || mMapView == null) {
                return;
            }

            MyLocationData locationData = new MyLocationData.Builder()
                    //设置精度圈
                    .accuracy(bdLocation.getRadius()/2)
                    //设置方向
                    .direction(bdLocation.getDirection())
                    .latitude(bdLocation.getLatitude())
                    .longitude(bdLocation.getLongitude())
                    .build();
            mBaiduMap.setMyLocationData(locationData);
            // 设置定位数据
            mBaiduMap.setMyLocationData(locationData);
            mCurrentAccracy=locationData.accuracy;
            mCurrentLantitude = locationData.latitude;
            mCurrentLongitude = locationData.longitude;
            // 设置自定义图标
            BitmapDescriptor mCurrentMarker = BitmapDescriptorFactory.fromResource(R.drawable.navi_map_gps_locked);
            MyLocationConfiguration config = new MyLocationConfiguration(mCurrentMode, true, mCurrentMarker);
            mBaiduMap.setMyLocationConfiguration(config);

            if (isFristLocation) {
                isFristLocation = false;
                LatLng ll = new LatLng(bdLocation.getLatitude(),bdLocation.getLongitude());
                MapStatus.Builder builder = new MapStatus.Builder();
                builder.target(ll).zoom(19.0f);
                mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
            }
        }
    }

    //检测GPS是否打开
    private boolean checkGPSIsOpen() {
        boolean isOpen;
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        isOpen = locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER);
        return isOpen;
    }
    //跳转GPS设置
    private void openGPSSettings() {
        if (checkGPSIsOpen()) {
            center2myLoc(); //自己写的定位方法
        }
        else { //没有打开则弹出对话框
            new AlertDialog.Builder(this) .setTitle(R.string.notifyTitle) .setMessage(R.string.gpsNotifyMsg) // 拒绝, 退出应用
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                }) .setPositiveButton(R.string.setting, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) { //跳转GPS设置界面
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivityForResult(intent, GPS_REQUEST_CODE);
                    }
                }) .setCancelable(false) .show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GPS_REQUEST_CODE) { //做需要做的事情，比如再次检测是否打开GPS了 或者定位
            openGPSSettings();
        }
    }

    private void useGPS()
    {
        // 定位初始化
        mLocationClient = new LocationClient(this);
        myMyBDAbstractLocationListener = new MyBDAbstractLocationListener();
        mLocationClient.registerLocationListener(myMyBDAbstractLocationListener);
        // 设置定位的相关配置
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true); // 打开gps
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setScanSpan(100);
        mLocationClient.setLocOption(option);
        mLocationClient.start();//开启定位
        mBaiduMap.setMyLocationEnabled(true);//开启定位图层
    }

    //初始化方向传感器
    private void initOritationListener() {
        myOrientationListener = new MyOrientationListener( getApplicationContext());
        myOrientationListener .setOnOrientationListener(new MyOrientationListener.OnOrientationListener() {
            @Override
            public void onOrientationChanged(float x) {
                mXDirection = (int) x; // 构造定位数据
                MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(mCurrentAccracy/2) // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(mXDirection)
                    .latitude(mCurrentLantitude)
                    .longitude(mCurrentLongitude).build(); // 设置定位数据
                mBaiduMap.setMyLocationData(locData); // 设置自定义图标
                BitmapDescriptor mCurrentMarker = BitmapDescriptorFactory.fromResource(R.drawable.navi_map_gps_locked);
                MyLocationConfiguration config = new MyLocationConfiguration(mCurrentMode, true, mCurrentMarker);
                mBaiduMap.setMyLocationConfiguration(config);
            }
        });
    }

    //地图移动到我的位置,此处可以重新发定位请求，然后定位；直接拿最近一次经纬度，如果长时间没有定位成功，可能会显示效果不好
    private void center2myLoc() {
         LatLng ll = new LatLng(mCurrentLantitude, mCurrentLongitude);
         MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
         mBaiduMap.animateMapStatus(u);
    }

    private double rad(double d)
    {
        return d * Math.PI / 180.0;
    }

    private double GetDistance(double lon1,double lat1,double lon2, double lat2)
    {
        double radLat1 = rad(lat1);
        double radLat2 = rad(lat2);
        double a = radLat1 - radLat2;
        double b = rad(lon1) - rad(lon2);
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a/2),2)+Math.cos(radLat1)*Math.cos(radLat2)*Math.pow(Math.sin(b/2),2)));
        s = s * EARTH_RADIUS;
        s = Math.round(s * 10000) / 10000;
        return s;
    }

    //绘制多个marker
    private void createPois(List<Integer> poi)
    {
        //创建OverlayOptions的集合
        List<OverlayOptions> options = new ArrayList<OverlayOptions>();
        //构建Marker图标
        BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(R.drawable.icon_geo);
        for(int i:poi)
        {
            //用来构造InfoWindow的Button
            Button button = new Button(getApplicationContext());
            button.setBackgroundResource(R.drawable.popup);
            button.setText(placeInfo.placeName.get(i));
            ColorStateList black =getResources().getColorStateList(R.color.black);
            button.setTextColor(black);
            //构造InfoWindow, point 描述的位置点, -100 InfoWindow相对于point在y轴的偏移量
            InfoWindow mInfoWindow = new InfoWindow(button, new LatLng(placeInfo.latitude[i],placeInfo.longitude[i]), -80);

            //使InfoWindow生效
            mBaiduMap.showInfoWindow(mInfoWindow);

            OverlayOptions option=createOption(i,bitmap);
            //将OverlayOptions添加到list
            options.add(option);
        }
        //在地图上批量添加
        mBaiduMap.addOverlays(options);
    }
    private OverlayOptions createOption(int i,BitmapDescriptor bitmap)
    {
        //构造大量坐标数据
        LatLng point = new LatLng(placeInfo.latitude[i], placeInfo.longitude[i]);
        //创建OverlayOptions属性
        OverlayOptions option =  new MarkerOptions().position(point).icon(bitmap);
        return option;
    }

    //单个
    private void createPoi(LatLng point,String info)
    {
        //构建Marker图标
        BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(R.drawable.icon_geo);
        //构建MarkerOption，用于在地图上添加Marker
        OverlayOptions option = new MarkerOptions().position(point).icon(bitmap);
        //在地图上添加Marker，并显示
        mBaiduMap.addOverlay(option);

        //用来构造InfoWindow的Button
        Button button = new Button(getApplicationContext());
        button.setBackgroundResource(R.drawable.popup);
        button.setText(info);
        ColorStateList black =getResources().getColorStateList(R.color.black);
        button.setTextColor(black);
        //构造InfoWindow, point 描述的位置点, -100 InfoWindow相对于point在y轴的偏移量
        InfoWindow mInfoWindow = new InfoWindow(button, point, -80);

        //使InfoWindow生效
        mBaiduMap.showInfoWindow(mInfoWindow);
    }

}



