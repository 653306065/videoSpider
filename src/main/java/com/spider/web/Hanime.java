package com.spider.web;

import java.net.Proxy;

import com.spider.constant.Constant;
import com.spider.entity.HanimeImage;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.spider.utils.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.spider.utils.OKHttpUtils;
import com.spider.utils.download.ImageDownload;
import org.springframework.util.CollectionUtils;
import com.spider.service.HanimeImageService;

@Service
public class Hanime extends BaseWeb {

    @Value("${hanime.imageApi}")
    private String imageApi;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    Proxy proxy;

    @Autowired
    ImageDownload imageDownload;

    @Value("${hanime.videoListApi}")
    private String videoListApi;

    @Value("${hanime.videoDownloadApi}")
    private String videoDownloadApi;

    @Value("${hanime.auth}")
    private String auth;

    @Value("${hanime.auth_expires}")
    private String auth_expires;

    @Autowired
    private HanimeImageService hanimeImageService;

    public void downloadImage(String channel) {
        String maxApi = imageApi.replace("@{channel}", channel).replace("@{beforeId}", "1999999");
        String json = OKHttpUtils.get(maxApi, enableProxy);
        if (Objects.isNull(json)) {
            return;
        }
        String key = "hanimeImageTask";
        new Thread(() -> {
            List<HanimeImage> hanimeImageList = JSONObject.parseObject(json).getJSONArray("data").toJavaList(HanimeImage.class);
            Integer maxId = hanimeImageList.stream().mapToInt(HanimeImage::getId).max().getAsInt();
            while (true) {
                String api = imageApi.replace("@{channel}", channel).replace("@{beforeId}", String.valueOf(maxId));
                logger.info(api);
                String jsonStr = OKHttpUtils.get(api, enableProxy);
                if (Objects.isNull(jsonStr)) {
                    continue;
                }
                hanimeImageList = JSONObject.parseObject(jsonStr).getJSONArray("data").toJavaList(HanimeImage.class);
                if (CollectionUtils.isEmpty(hanimeImageList)) {
                    break;
                }
                redisTemplate.opsForList().rightPushAll(key, hanimeImageList.stream().map(JSON::toJSONString).collect(Collectors.toList()));
                maxId = hanimeImageList.stream().mapToInt(HanimeImage::getId).min().getAsInt();
            }
        }).start();

        ExecutorService executorService = Executors.newFixedThreadPool(getThread());
        for (int i = 0; i < getThread(); i++) {
            executorService.execute(() -> {
                while (true) {
                    String jsonStr = redisTemplate.opsForList().leftPop(key, 1, TimeUnit.SECONDS);
                    HanimeImage hanimeImage = JSON.parseObject(jsonStr, HanimeImage.class);
                    if (Objects.nonNull(hanimeImage) && Objects.nonNull(hanimeImageService.findById(hanimeImage.getId()))) {
                        logger.info("id:{},url:{},已存在", hanimeImage.getId(), hanimeImage.getUrl());
                        continue;
                    }
                    if (Objects.nonNull(hanimeImage) && Objects.nonNull(hanimeImage.getUrl())) {
                        byte[] bytes = OKHttpUtils.getBytes(hanimeImage.getUrl(),enableProxy);
                        if (Objects.isNull(bytes)) {
                            logger.info("id:{},url:{},获取为空", hanimeImage.getId(), hanimeImage.getUrl());
                            //redisTemplate.opsForList().leftPush(key,jsonStr);
                            continue;
                        }
                        hanimeImage.setMd5(md5.digestHex(bytes));
                        if( Objects.nonNull(hanimeImageService.findOnekeyValue("md5",hanimeImage.getMd5()))){
                            logger.info("id:{},url:{},md5重复", hanimeImage.getId(), hanimeImage.getUrl());
                            continue;
                        }
                        String path = savePath + fileSeparator + hanimeImage.getChannelName() + fileSeparator + hanimeImage.getId() + "." + hanimeImage.getExtension();
                        FileUtils.byteToFile(bytes, path);
                        hanimeImageService.insert(hanimeImage);
                        logger.info("id:{},url:{},下载完成", hanimeImage.getId(), hanimeImage.getUrl());
                    }
                }
            });
        }

    }

    public void download_nsfw_general_Image() {
        downloadImage("nsfw-general");
    }

    public void Download_yuri_Image() {
        downloadImage("yuri");
    }

    public void Download_irl_3d_Image() {
        downloadImage("irl-3d");
    }

    public List<String> getVideoList(int page) {
        List<String> list = new ArrayList<String>();
        String json = "{\"search_text\":\"\",\"tags\":[],\"brands\":[],\"blacklist\":[],\"order_by\":\"created_at_unix\",\"ordering\":\"desc\",\"page\":"
                + page + "}";
        String data = OKHttpUtils.postJson(videoListApi, null, json, enableProxy);
        JSONObject JSONObject = JSON.parseObject(data);
        String hits = JSONObject.getString("hits");
        JSONArray array = JSON.parseArray(hits);
        for (int i = 0; i < array.size(); i++) {
            list.add(array.getString(i));
        }
        return list;
    }

    public String getVideoUrl(String slug) {
        String api = videoDownloadApi.replace("@{slug}", slug);
        String json = "{\"auth_kind\":\"saved_download_token\",\"auth\":\"" + auth + "\",\"auth_expires\":\""
                + auth_expires + "\"}";
        String data = OKHttpUtils.postJson(api, null, json, enableProxy);
        if (data == null) {
            return null;
        }
        JSONObject dataJson = JSON.parseObject(data);
        JSONObject videos_manifest = dataJson.getJSONObject("videos_manifest");
        if (videos_manifest == null) {
            return null;
        }
        JSONArray servers = videos_manifest.getJSONArray("servers");
        if (servers == null) {
            return null;
        }
        JSONArray streams = servers.getJSONObject(0).getJSONArray("streams");
        if (streams == null) {
            return null;
        }
        String url = "";
        for (int i = 0; i < streams.size(); i++) {
            url = streams.getJSONObject(i).getString("url");
            if (url.length() != 0) {
                break;
            }
        }
        return url;
    }

    public void downloadVideo() {
        int startPage = 0;
        while (true) {
            try {
                List<String> videoInfoList = getVideoList(startPage);
                for (String video : videoInfoList) {
                    JSONObject videoJson = JSON.parseObject(video);
                    String slug = videoJson.getString("slug");
                    String name = videoJson.getString("name") + ".mp4";
                    String url = getVideoUrl(slug);
                    if (url == null) {
                        continue;
                    }
                    String path = savePath + "\\video\\" + name;
                    multithreadingDownload.fileDownload(url, path, null, enableProxy, thread, 1024 * 1024 * 5);
                    logger.info(url);
                }
                if (videoInfoList.size() == 0) {
                    break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
