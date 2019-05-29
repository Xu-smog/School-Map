package com.xu.school_map;

import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.route.DrivingRoutePlanOption;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.WalkingRoutePlanOption;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

public class DetailsActivity extends AppCompatActivity {

    private String audio;
    private MediaPlayer mediaPlayer;
    private boolean play=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        Intent intent=getIntent();
        String name=intent.getStringExtra("name");

        setContentView(R.layout.activity_details);

        TextView textView= findViewById(R.id.titleView);
        TextView descriptionView = findViewById(R.id.descriptionView);
        ImageView imageView=findViewById(R.id.imageView);

        textView.setText(name);

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
                descriptionView.setText(text);
                audio=place.getString("audio");
                JSONArray imageArray=place.getJSONArray("images");
                for(int i=0;i<imageArray.length();i++) {
                    InputStream inputStream = getResources().getAssets().open(imageArray.getString(i));
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    if(i==0) {
                        imageView.setImageBitmap(bitmap);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }


        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //按钮的单击事件
    public void onClick(View view) {
        switch (view.getId()) {
            //切换地图类型
            case R.id.audioButton:
                if(play) {
                    mediaPlayer.stop();
                    play=false;
                }
                else {
                    playSoundByMedia();
                    play=true;
                }
                break;
        }
    }

    private void playSoundByMedia() {
        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            //mediaPlayer.setOnCompletionListener(beepListener);
            try {
                AssetFileDescriptor file = getResources().getAssets().openFd(audio);
                mediaPlayer.setDataSource(file.getFileDescriptor(), file.getStartOffset(), file.getLength());
                file.close();
                mediaPlayer.setVolume(0.50f, 0.50f);
                mediaPlayer.prepare();
                mediaPlayer.start();
            }
            catch (IOException e) {
                mediaPlayer = null;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

}