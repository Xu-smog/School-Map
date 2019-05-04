package com.xu.school_map;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlaceInfo {
    public final List<String> placeName = new ArrayList<String>(){{
        add("西院-图书馆");add("西院-博远楼");add("西院-博知楼");add("西院-博文楼");add("西院-博学楼");
    }};
    /*
    public final String[] placeName=new String[]{
        "西院-图书馆","西院-博远楼","西院-博知楼","西院-博文楼","西院-博学楼"
    };*/
    public final Map<String, Integer> placeMap=new HashMap<String,Integer>(){{
        put("西院-图书馆",0);put("西院-博远楼",1);put("西院-博知楼",2);put("西院-博文楼",3);put("西院-博学楼",4);
    }};
    //纬度
    public final double[] latitude=new double[]{
        36.078445,36.077625,36.076374,36.075638,36.075032
    };
    //经度
    public final double[] longitude=new double[]{
        120.426168,120.428041,120.427929,120.428647,120.429357
    };

}
