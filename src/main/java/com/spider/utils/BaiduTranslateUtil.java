package com.spider.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class BaiduTranslateUtil {

    private static final String TRANS_API_HOST = "http://api.fanyi.baidu.com/api/trans/vip/translate";

    public static String appId = "20180427000151353";

    public static String secret = "8yOZfDY2nQeCp9oIk_VS";

    public static String translate(String text, String resourceLanguage, String targetLanguage) {
        String json = getTransResult(text, resourceLanguage, targetLanguage);
        if (Objects.isNull(json)) {
            return null;
        }
        JSONObject jsonObject = JSON.parseObject(json);
        JSONArray jsonArray = jsonObject.getJSONArray("trans_result");
        String translateText = "";
        for (int i = 0; i < jsonArray.size(); i++) {
            translateText = jsonArray.getJSONObject(i).getString("dst");
        }
        return translateText;
    }

    public static String getTransResult(String query, String from, String to) {
        Map<String, String> params = buildParams(query, from, to);
        return OKHttpUtils.get(TRANS_API_HOST, params, null, false);
    }

    private static Map<String, String> buildParams(String query, String from, String to) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("q", query);
        params.put("from", from);
        params.put("to", to);
        params.put("appid", appId);
        String salt = String.valueOf(System.currentTimeMillis());
        params.put("salt", salt);
        String src = appId + query + salt + secret;
        params.put("sign", MD5Util.md5(src));
        return params;
    }

    public static void main(String[] args) {
        String text = translate("10周年記念特別ｺﾚｸﾀｰｽﾞｱｲﾃﾑ vol.8", "jp", "zh");
        System.out.println(text);
    }
}
