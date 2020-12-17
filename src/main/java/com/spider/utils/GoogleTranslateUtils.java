package com.spider.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GoogleTranslateUtils {

    static Logger logger = LoggerFactory.getLogger(GoogleTranslateUtils.class);

    public static final String api = "http://translate.google.cn/translate_a/single?client=gtx&dt=t&dj=1&ie=UTF-8&sl=@{sourceLanguage}&tl=@{targetLanguage}&q=";

    public static String translate(String text, String sourceLanguage, String targetLanguage) {
        String realUrl = api.replace("@{sourceLanguage}", sourceLanguage).replace("@{targetLanguage}", targetLanguage) + text;
        String json = OKHttpUtils.get(realUrl, false);
        if (StringUtils.isBlank(json)) {
            return null;
        }
        JSONObject jsonObject = JSON.parseObject(json);
        JSONArray sentences = jsonObject.getJSONArray("sentences");
        String translateText = "";
        for (int i = 0; i < sentences.size(); i++) {
            JSONObject trans = sentences.getJSONObject(i);
            translateText = translateText + trans.getString("trans");
        }
        //logger.info(json);
        return translateText;
    }

    public static void main(String[] args) {
        System.out.println(translate("メルシーボークー MXX 32 ぶっかけ中出しアナル : 古瀬玲", "ja", "zh"));
    }

}
