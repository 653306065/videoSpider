package com.spider.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.spider.entity.FaceInfo;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import java.util.HashMap;
import java.util.Map;

public class FaceUtil {

    public static final String apiKey = "ANVDjMVzjAu-w-zyD9f0KZzEznkoRWnC";

    public static final String apiSecret = "jK7PpBZn3ZMZyg_7piODby5GR3J1t5p8";

    public static final String api="https://api-cn.faceplusplus.com/facepp/v3/detect";

    public static FaceInfo faceInfo(byte[] imageByte) {
        Map<String, Object> map = new HashMap<>(5);
        map.put("api_key", apiKey);
        map.put("api_secret", "apiSecret");
        map.put("image_file", imageByte);
        map.put("beauty_score_max", 100);
        map.put("return_attributes", "beauty,age,gender");
        String json = OKHttpUtils.postFormObjectData(api, map, false);
        if (!StringUtils.isEmpty(json)) {
            JSONObject jsonObject = JSON.parseObject(json);
            JSONArray jsonArray = jsonObject.getJSONArray("faces");
            if (!CollectionUtils.isEmpty(jsonArray)) {
                String gender = jsonArray.getJSONObject(0).getJSONObject("attributes").getJSONObject("gender").getString("value");
                Integer age = jsonArray.getJSONObject(0).getJSONObject("attributes").getJSONObject("age").getInteger("value");
                double male_score = jsonArray.getJSONObject(0).getJSONObject("attributes").getJSONObject("beauty").getDouble("male_score");
                double female_score = jsonArray.getJSONObject(0).getJSONObject("attributes").getJSONObject("beauty").getDouble("female_score");
                FaceInfo faceInfo = FaceInfo.builder().age(age).gender(gender).maleScore(male_score).femaleScore(female_score).build();
                return faceInfo;
            } else {
                return null;
            }
        }
        return null;
    }
}
