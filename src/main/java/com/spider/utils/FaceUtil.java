package com.spider.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.spider.entity.FaceInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FaceUtil {

    public static Logger logger = LoggerFactory.getLogger(FaceUtil.class);

    public static final String apiKey = "ANVDjMVzjAu-w-zyD9f0KZzEznkoRWnC";

    public static final String apiSecret = "jK7PpBZn3ZMZyg_7piODby5GR3J1t5p8";

    public static final String api = "https://api-cn.faceplusplus.com/facepp/v3/detect";

    public static List<FaceInfo> faceInfo(byte[] imageByte) {
        Map<String, Object> map = new HashMap<>(5);
        map.put("api_key", apiKey);
        map.put("api_secret", apiSecret);
        map.put("image_file", imageByte);
        map.put("beauty_score_max", 100);
        map.put("return_attributes", "beauty,age,gender");
        String json = OKHttpUtils.postFormObjectData(api, map, false);
        //logger.info(json);
        if (!StringUtils.isEmpty(json)) {
            JSONObject jsonObject = JSON.parseObject(json);
            JSONArray jsonArray = jsonObject.getJSONArray("faces");
            if (!CollectionUtils.isEmpty(jsonArray)) {
                List<FaceInfo> list = new ArrayList<>();
                for (int i = 0; i < jsonArray.size(); i++) {
                    String gender = jsonArray.getJSONObject(0).getJSONObject("attributes").getJSONObject("gender").getString("value");
                    Integer age = jsonArray.getJSONObject(0).getJSONObject("attributes").getJSONObject("age").getInteger("value");
                    double maleScore = jsonArray.getJSONObject(0).getJSONObject("attributes").getJSONObject("beauty").getDouble("male_score");
                    double femaleScore = jsonArray.getJSONObject(0).getJSONObject("attributes").getJSONObject("beauty").getDouble("female_score");
                    FaceInfo faceInfo = FaceInfo.builder().age(age).gender(gender).maleScore(maleScore).femaleScore(femaleScore).build();
                    list.add(faceInfo);
                }
                return list;
            } else {
                return null;
            }
        }
        return null;
    }
}
