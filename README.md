# School-Map
校园地图

基于百度地图SDK

本项目借鉴了网上许多作者的作品，感谢他们的分享，在这里我也整理一下我的项目

1.获得秘钥

。。。

2.地图基本选项
    
    private MapView mMapView = null;
    private BaiduMap mBaiduMap;
    
    mMapView = (MapView) findViewById(R.id.bmapView);
    mBaiduMap=mMapView.getMap();
    
  （1）缩放级别
        
        MapStatus.Builder builder = new MapStatus.Builder();
        builder.zoom(19.0f);
        mBaiduMap.setMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
        
  （2）设置地图中心
        
        GEO为LatLng的一个实例
        builder.target(GEO);

   （3） 隐藏百度的LOGO
        
        View child = mMapView.getChildAt(1);
        if (child != null && (child instanceof ImageView || child instanceof ZoomControls)) {
            child.setVisibility(View.INVISIBLE);
        }

3.权限
  （1）在AndroidManifest.xml文件中
  
    <uses-permission android:name="com.android.launcher.permission.READ_SETTINGS" />
    <uses-permission android:name="android.permission.WAKE_LOCK" />
    <uses-permission android:name="android.permission.GET_ACCOUNTS" />
    <uses-permission android:name="android.permission.USE_CREDENTIALS" />
    <uses-permission android:name="android.permission.MANAGE_ACCOUNTS" />
    <uses-permission android:name="android.permission.AUTHENTICATE_ACCOUNTS" />
    <uses-permission android:name="com.android.launcher.permission.READ_SETTINGS" />
    <uses-permission android:name="android.permission.BROADCAST_STICKY" />
    <uses-permission android:name="android.permission.WRITE_SETTINGS" />
    <uses-permission android:name="android.ACCESS_GPS" />
    <uses-permission android:name="com.android.launcher.permission.READ_SETTINGS"/>
    <!-- 获取CUID，用于鉴权，请求签名等 -->
    <uses-permission android:name="com.android.launcher.permission.WRITE_SETTINGS" />
    <!-- 这个权限用于进行网络定位 -->
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"/>
    <!-- 这个权限用于访问GPS定位 -->
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
    <!-- 用于访问wifi网络信息，wifi信息会用于进行网络定位 -->
    <uses-permission android:name="android.permission.ACCESS_WIFI_STATE"/>
    <!-- 获取运营商信息，用于支持提供运营商信息相关的接口 -->
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>
    <!-- 用于读取手机当前的状态 -->
    <uses-permission android:name="android.permission.READ_PHONE_STATE"/>
    <!-- 写入扩展存储，向扩展卡写入数据，用于写入离线定位数据 -->
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
    <!-- 访问网络，网络定位需要上网 -->
    <uses-permission android:name="android.permission.INTERNET"/>
    <uses-permission android:name="android.permission.CAMERA" />
    <uses-permission android:name="android.permission.VIBRATE"/>
    
  （2）动态申请权限（在MainActivity.java文件中）
  
    //权限
    private static boolean isPermissionRequested = false
    
    //Android6.0之后需要动态申请权限
    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= 23 && !isPermissionRequested) {
            isPermissionRequested = true;
            ArrayList<String> permissionsList = new ArrayList<>();

            String[] permissions = {
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.ACCESS_NETWORK_STATE,
                    Manifest.permission.INTERNET,
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.MODIFY_AUDIO_SETTINGS,
                    Manifest.permission.WRITE_SETTINGS,
                    Manifest.permission.ACCESS_WIFI_STATE,
                    Manifest.permission.CHANGE_WIFI_STATE,
                    Manifest.permission.CHANGE_WIFI_MULTICAST_STATE
            };

            for (String perm : permissions) {
                if (PackageManager.PERMISSION_GRANTED != checkSelfPermission(perm)) {
                    permissionsList.add(perm);
                    // 进入到这里代表没有权限.
                }
            }

            if (permissionsList.isEmpty()) {
                return;
            }
            else {
                requestPermissions(permissionsList.toArray(new String[permissionsList.size()]), 0);
            }
        }
    }
    
   （3）获取GPS权限
    如果没有打开GPS，跳转到相应的页面
    
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
    
4.定位

    //定位的监听器
    public MyBDAbstractLocationListener myMyBDAbstractLocationListener;
    //当前定位的模式
    private MyLocationConfiguration.LocationMode mCurrentMode = MyLocationConfiguration.LocationMode.NORMAL;;
    private int GPS_REQUEST_CODE = 10;
    // 最新一次的经纬度
    private double mCurrentLatitude;
    private double mCurrentLongitude;
    
    @Override
    protected void onStart() { 
        // 开启图层定位
        mBaiduMap.setMyLocationEnabled(true);
        if (!mLocationClient.isStarted()) {
            mLocationClient.start();
        } // 开启方向传感器
        myOrientationListener.start(); 
        super.onStart();
    }

    @Override protected void onStop() { 
        // 关闭图层定位
        mBaiduMap.setMyLocationEnabled(false); mLocationClient.stop(); 
        // 关闭方向传感器
        myOrientationListener.stop();
        super.onStop();
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
            mCurrentLatitude = locationData.latitude;
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
    
    private void useGPS() {
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
    
    //地图移动到我的位置,此处可以重新发定位请求，然后定位；直接拿最近一次经纬度，如果长时间没有定位成功，可能会显示效果不好
    private void center2myLoc() {
         LatLng ll = new LatLng(mCurrentLatitude, mCurrentLongitude);
         MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
         mBaiduMap.animateMapStatus(u);
    }

5.获取手机旋转传感器信息
    定义了一个类，详见School-Map/School_Map/app/src/main/java/com/xu/school_map/MyOrientationListener.java
    
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
                    .latitude(mCurrentLatitude)
                    .longitude(mCurrentLongitude).build(); // 设置定位数据
                mBaiduMap.setMyLocationData(locData); // 设置自定义图标
                BitmapDescriptor mCurrentMarker = BitmapDescriptorFactory.fromResource(R.drawable.navi_map_gps_locked);
                MyLocationConfiguration config = new MyLocationConfiguration(mCurrentMode, true, mCurrentMarker);
                mBaiduMap.setMyLocationConfiguration(config);
                stNode=PlanNode.withLocation(new LatLng(mCurrentLatitude,mCurrentLongitude));
            }
        });
    }
    
6.输入栏

    //搜索关键字输入窗口
    private AutoCompleteTextView keyWordsView = null;
    private myAdapter<String> sugAdapter = null;
    //这是我自己定义的存储信息的类
    private PlaceInfo placeInfo=new PlaceInfo();
    
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
        createPoi(poi,placeInfo.placeName.get(index));
        enNode=PlanNode.withLocation(poi);
        endPt=poi;
        mBaiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(poi));
        }
    });
    
7.路线规划
    这部分代码参考百度地图的demo,找到对应的demo将res文件夹中的文件拷贝过来，找到baidu/mapapi文件夹里面包含clusterutil和overlayutil文件夹，直接将baidu文件夹拷贝到com文件夹下面
    
    private PlanNode stNode;
    private PlanNode enNode;
    private RoutePlanSearch mSearch;
    private OnGetRoutePlanResultListener onGetRoutePlanResultListener;
    
        mSearch = RoutePlanSearch.newInstance();
        onGetRoutePlanResultListener = new OnGetRoutePlanResultListener() {
            @Override
            public void onGetWalkingRouteResult(WalkingRouteResult result) {
                if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
                    Toast.makeText(MainActivity.this, "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
                }
                if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
                    //起终点或途经点地址有岐义，通过以下接口获取建议查询信息
                    //result.getSuggestAddrInfo()
                    Toast.makeText(MainActivity.this, "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (result.error == SearchResult.ERRORNO.NO_ERROR) {
                    //创建WalkingRouteOverlay实例
                    WalkingRouteOverlay overlay = new WalkingRouteOverlay(mBaiduMap);
                    if (result.getRouteLines().size() > 0) {
                        //获取路径规划数据,(以返回的第一条数据为例)
                        //为WalkingRouteOverlay实例设置路径数据
                        overlay.setData(result.getRouteLines().get(0));
                        //在地图上绘制WalkingRouteOverlay
                        mBaiduMap.clear();
                        overlay.addToMap();
                    }
                }
            }
            @Override
            public void onGetTransitRouteResult(TransitRouteResult transitRouteResult) {

            }
            @Override
            public void onGetMassTransitRouteResult(MassTransitRouteResult massTransitRouteResult) {

            }
            @Override
            public void onGetDrivingRouteResult(DrivingRouteResult result) {
                if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
                    Toast.makeText(MainActivity.this, "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
                }
                if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
                    //起终点或途经点地址有岐义，通过以下接口获取建议查询信息
                    //result.getSuggestAddrInfo()
                    Toast.makeText(MainActivity.this, "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (result.error == SearchResult.ERRORNO.NO_ERROR) {
                    DrivingRouteOverlay overlay = new DrivingRouteOverlay(mBaiduMap);//路线覆盖物，MyDrivingRouteOverlay代码下面给出
                    overlay.setData(result.getRouteLines().get(0));
                    overlay.addToMap();
                }
            }
            @Override
            public void onGetIndoorRouteResult(IndoorRouteResult indoorRouteResult) {

            }
            @Override
            public void onGetBikingRouteResult(BikingRouteResult bikingRouteResult) {

            }
        };

        mSearch.setOnGetRoutePlanResultListener(onGetRoutePlanResultListener);
    
    //起点
    stNode=PlanNode.withLocation(new LatLng(mCurrentLatitude,mCurrentLongitude));
    //终点
    enNode=PlanNode.withLocation(point);
    mSearch.walkingSearch((new WalkingRoutePlanOption()).from(stNode).to(enNode));

8.导航
    这部分代码来自百度地图的demo,注意一定要使用和demo相同版本的SDK，如果使用旧版本的将无法成功使用
    将res文件夹中的文件拷贝过来

    private LatLng startPt;
    private LatLng endPt=null;

    private BikeNaviLaunchParam bikeParam;
    private WalkNaviLaunchParam walkParam;
    
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
            case R.id.leftBottomButton:
                if(endPt==null) {
                    Toast.makeText(MainActivity.this, "请在输入栏选择目的地", Toast.LENGTH_LONG).show();
                    break;
                }
                startPt=new LatLng(mCurrentLatitude,mCurrentLongitude);
                WalkRouteNodeInfo walkStartNode = new WalkRouteNodeInfo();
                walkStartNode.setLocation(startPt);
                WalkRouteNodeInfo walkEndNode = new WalkRouteNodeInfo();
                walkEndNode.setLocation(endPt);
                walkParam = new WalkNaviLaunchParam().startNodeInfo(walkStartNode).endNodeInfo(walkEndNode);
                walkParam.extraNaviMode(0);
                startWalkNavi();
                //构造导航起终点参数对象
                /*
                stNode=PlanNode.withLocation(new LatLng(mCurrentLatitude,mCurrentLongitude));
                mSearch.walkingSearch((new WalkingRoutePlanOption()).from(stNode).to(enNode));*/
                break;
            case R.id.rightBottomButton:
                if(endPt==null) {
                    Toast.makeText(MainActivity.this, "请在输入栏选择目的地", Toast.LENGTH_LONG).show();
                    break;
                }
                startPt=new LatLng(mCurrentLatitude,mCurrentLongitude);
                BikeRouteNodeInfo bikeStartNode = new BikeRouteNodeInfo();
                bikeStartNode.setLocation(startPt);
                BikeRouteNodeInfo bikeEndNode = new BikeRouteNodeInfo();
                bikeEndNode.setLocation(endPt);
                bikeParam = new BikeNaviLaunchParam().startNodeInfo(bikeStartNode).endNodeInfo(bikeEndNode);
                /*
                stNode=PlanNode.withLocation(new LatLng(mCurrentLatitude,mCurrentLongitude));
                mSearch.drivingSearch((new DrivingRoutePlanOption()).from(stNode).to(enNode));*/

                break;
        }
    }
    
    /**
     * 开始骑行导航
     */
    private void startBikeNavi() {

        try {
            BikeNavigateHelper.getInstance().initNaviEngine(this, new IBEngineInitListener() {
                @Override
                public void engineInitSuccess() {

                    routePlanWithBikeParam();
                }

                @Override
                public void engineInitFail() {

                    BikeNavigateHelper.getInstance().unInitNaviEngine();
                }
            });
        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    /**
     * 开始步行导航
     */
    private void startWalkNavi() {

        try {
            WalkNavigateHelper.getInstance().initNaviEngine(this, new IWEngineInitListener() {
                @Override
                public void engineInitSuccess() {

                    routePlanWithWalkParam();
                }

                @Override
                public void engineInitFail() {

                    WalkNavigateHelper.getInstance().unInitNaviEngine();
                }
            });
        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    /**
     * 发起骑行导航算路
     */
    private void routePlanWithBikeParam() {
        BikeNavigateHelper.getInstance().routePlanWithRouteNode(bikeParam, new IBRoutePlanListener() {
            @Override
            public void onRoutePlanStart() {

            }

            @Override
            public void onRoutePlanSuccess() {

                Intent intent = new Intent();
                intent.setClass(MainActivity.this, BNaviGuideActivity.class);
                startActivity(intent);
            }

            @Override
            public void onRoutePlanFail(BikeRoutePlanError error) {

            }

        });
    }

    /**
     * 发起步行导航算路
     */
    private void routePlanWithWalkParam() {
        WalkNavigateHelper.getInstance().routePlanWithRouteNode(walkParam, new IWRoutePlanListener() {
            @Override
            public void onRoutePlanStart() {

            }

            @Override
            public void onRoutePlanSuccess() {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, WNaviGuideActivity.class);
                startActivity(intent);

            }

            @Override
            public void onRoutePlanFail(WalkRoutePlanError error) {

            }

        });
    }

9.自定义infoWindow
    见School-Map/School_Map/app/src/main/res/layout/info_window_view.xml文件
    
    private void getInfoWindoView(@NonNull Marker marker) {
        View infoView = LayoutInflater.from(this).inflate(R.layout.info_window_view, null);

        TextView infoTitle = infoView.findViewById(R.id.HeadTextView);
        TextView infoDetail = infoView.findViewById(R.id.ContentTextView);
        LinearLayout layoutInfo = infoView.findViewById(R.id.InfoLinerlayout);
        ImageView navigation = infoView.findViewById(R.id.GoThereView);

        final Bundle bundle=marker.getExtraInfo();
        final LatLng point=new LatLng(bundle.getDouble("latitude"),bundle.getDouble("longitude"));
        infoTitle.setText(bundle.getString("name"));
        infoDetail.setText(bundle.getString("info"));

        layoutInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainActivity.this,DetailsActivity.class);
                intent.putExtra("name",bundle.getString("name"));
                startActivity(intent);
            }
        });
        navigation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(MainActivity.this, "Click on navigation", Toast.LENGTH_LONG).show();

                stNode=PlanNode.withLocation(new LatLng(mCurrentLatitude,mCurrentLongitude));
                enNode=PlanNode.withLocation(point);
                //Toast.makeText(MainActivity.this, "Click on navigation"+enNode.getLocation().latitude, Toast.LENGTH_LONG).show();
                mSearch.walkingSearch((new WalkingRoutePlanOption()).from(stNode).to(enNode));
            }
        });
        //使InfoWindow生效
        mBaiduMap.showInfoWindow(new InfoWindow(infoView, point, -80));
    }
    
10.json文件读取

    private String getInfo(String name) {
        //Json数据的读写
        try {
            InputStreamReader inputStreamReader = new InputStreamReader(getResources().getAssets().open("placeInfo.json"), "UTF-8");
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line;
            StringBuilder builder = new StringBuilder();
            while ((line = bufferedReader.readLine()) != null) {
                builder.append(line);
            }
            inputStreamReader.close();
            bufferedReader.close();

            try {
                JSONObject jsonObject = new JSONObject(builder.toString());
                JSONObject place = jsonObject.getJSONObject(name);
                String text= place.getString("text");
                return text;
            } catch (JSONException e) {
                e.printStackTrace();
            }


        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

11.地图单击事件监听

    //设置地图单击事件监听
    mBaiduMap.setOnMapClickListener(new BaiduMap.OnMapClickListener() {
        //地图单击事件回调函数
        @Override
        public void onMapClick(LatLng point) {
          //自己定义操作
            List<Integer> poi=new ArrayList<Integer>();
            for(int i=0;i<placeInfo.latitude.length;i++) {
                if(DistanceUtil.getDistance(point, new LatLng(placeInfo.latitude[i],placeInfo.longitude[i]))<100) {
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
      
12.绘制Marker
   （1）绘制
   
    //绘制多个marker
    private void createPois(@NonNull List<Integer> poi) {
        //创建OverlayOptions的集合
        List<OverlayOptions> options = new ArrayList<OverlayOptions>();
        //构建Marker图标
        BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(R.drawable.icon_geo);
        for(int i:poi) {
            OverlayOptions option=createOption(i,bitmap);
            //将OverlayOptions添加到list
            options.add(option);
        }
        //在地图上批量添加
        mBaiduMap.addOverlays(options);
    }

    private OverlayOptions createOption(int i,BitmapDescriptor bitmap) {
        //构造大量坐标数据
        LatLng point = new LatLng(placeInfo.latitude[i], placeInfo.longitude[i]);
        //创建OverlayOptions属性
        Bundle bundle = new Bundle();
        bundle.putString("name", placeInfo.placeName.get(i));
        bundle.putString("info",getInfo(placeInfo.placeName.get(i)));
        bundle.putDouble("latitude",placeInfo.latitude[i]);
        bundle.putDouble("longitude",placeInfo.longitude[i]);
        OverlayOptions option =  new MarkerOptions().position(point).icon(bitmap).extraInfo(bundle);
        return option;
    }

    //单个
    private void createPoi(@NonNull LatLng point, String name) {
        //构建Marker图标
        BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(R.drawable.icon_geo);
        //构建MarkerOption，用于在地图上添加Marker
        Bundle bundle = new Bundle();
        bundle.putString("name", name);
        bundle.putString("info",getInfo(name));
        bundle.putDouble("latitude",point.latitude);
        bundle.putDouble("longitude",point.longitude);
        OverlayOptions option = new MarkerOptions().position(point).icon(bitmap).extraInfo(bundle);
        //在地图上添加Marker，并显示
        mBaiduMap.addOverlay(option);
    }

   （2）点击事件
   
           mBaiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
            //marker被点击时回调的方法,若响应点击事件，返回true，否则返回false,默认返回false
            @Override
            public boolean onMarkerClick(Marker marker) {
                getInfoWindoView(marker);
                return false;
            }
        });


  
  
  
  
  
