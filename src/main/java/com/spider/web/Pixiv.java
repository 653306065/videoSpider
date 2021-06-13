package com.spider.web;

import java.io.File;
import java.net.Proxy;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import com.spider.constant.Constant;
import com.spider.entity.HanimeImage;
import com.spider.entity.PixivImage;
import com.spider.service.HanimeImageService;
import com.spider.service.PixivImageService;
import com.spider.utils.FileUtils;
import com.spider.utils.JsoupUtil;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.springframework.util.StringUtils;

@Service
public class Pixiv extends BaseWeb {

    private Logger logger = LoggerFactory.getLogger(Pixiv.class);

    @Value("${pixiv.todayRank.R-18}")
    private String R18Url;

    @Value("${pixiv.cookie}")
    private String cookie;

    @Value("${pixiv.templatePage}")
    private String templatePage;

    @Value("${pixiv.newApi}")
    private String newApi;

    @Autowired
    Proxy proxy;

    @Autowired
    ImageDownload imageDownload;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private PixivImageService pixivImageService;

    @Autowired
    private HanimeImageService hanimeImageService;

    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");

    public List<String> getHistoryRankListUrl(Date date) {
        List<String> urlList = new ArrayList<>();
        String dateStr = simpleDateFormat.format(date);
        Map<String, String> header = new HashMap<>();
        header.put("cookie", cookie);
        header.put("user-agent", Constant.user_agent);
        int page = 1;
        while (true) {
            String url = R18Url.replace("@{date}", dateStr).replace("@{page}", String.valueOf(page));
            logger.info(url);
            String json = OKHttpUtils.get(url, header, enableProxy);
            if (!StringUtils.hasText(json)) {
                break;
            }
            JSONObject jsonObject = JSON.parseObject(json);
            JSONArray jsonArray = jsonObject.getJSONArray("contents");
            for (int i = 0; i < jsonArray.size(); i++) {
                urlList.add(jsonArray.getJSONObject(i).getString("illust_id"));
            }
            if (jsonObject.getString("next").equals("false")) {
                break;
            }
            page++;
        }
        if (CollectionUtils.isEmpty(urlList)) {
            return null;
        }
        return urlList;
    }

    public PixivImage getPixivImageByPageId(String id) {
        if (Objects.isNull(id)) {
            return null;
        }
        String url = templatePage.replace("@{id}", id);
        Document document = JsoupUtil.getDocument(url, enableProxy);
        if (Objects.isNull(document)) {
            return null;
        }
        String json = document.getElementById("meta-preload-data").attr("content");
        JSONObject jsonObject = JSON.parseObject(json);
        PixivImage image = jsonObject.getJSONObject("illust").getJSONObject(id).toJavaObject(PixivImage.class);
        List<String> urlList = new ArrayList<>();
        String original = image.getUrls().getOriginal();
        urlList.add(original);
        for (int i = 1; i < image.getPageCount(); i++) {
            urlList.add(original.replace("_p0", "_p" + i));
        }
        image.setImageUrl(urlList);
        return image;
    }

    public void downloadImage(PixivImage image) {
        if (Objects.isNull(image) || CollectionUtils.isEmpty(image.getImageUrl())) {
            return;
        }
        Map<String, String> header = new HashMap<>();
        header.put("Referer", "https://www.pixiv.net/ranking.php?mode=daily&content=illust");
        AtomicInteger i = new AtomicInteger();
        List<String> list = new ArrayList<>();
        image.setImageSavePath(image.getImageUrl().stream().sequential().map(url -> {
            //重试3次
            for (int index = 0; index < 3; index++) {
                byte[] bytes = OKHttpUtils.getBytes(url, header, enableProxy);
                if (Objects.nonNull(bytes)) {
                    String path = savePath + fileSeparator + image.getId() + "_" + i.get() + ".jpg";
                    String md5Str = md5.digestHex(bytes);
                    list.add(md5Str);
                    FileUtils.byteToFile(bytes, path);
                    logger.info("{},下载完成", path);
                    i.getAndIncrement();
                    HanimeImage hanimeImage = hanimeImageService.findOnekeyValue("md5", md5Str);
                    if (Objects.nonNull(hanimeImage)) {
                        new File(hanimeImage.getSavePath()).delete();
                        logger.info("hanimeImage:{},{},md5一致,删除文件", hanimeImage.getId(), hanimeImage.getUrl());
                    }
                    return path;
                }
            }
            return null;
        }).filter(Objects::nonNull).collect(Collectors.toList()));
        if (!CollectionUtils.isEmpty(list)) {
            image.setMd5List(list);
        }
        logger.info("{},下载完成", image.getTitle());
    }


    public void downloadNewImage() {
        String key = "pixivTask";
        Map<String, String> header = new HashMap<>();
        header.put("cookie", cookie);
        header.put("user-agent", Constant.user_agent);
        String api = newApi.replace("&lastId=@{lastId}", "").replace("@{type}", "illust");
        String json = OKHttpUtils.get(api, header, enableProxy);
        if (Objects.isNull(json)) {
            logger.info("获取最大Id失败");
            return;
        }
        JSONObject jsonObject = JSON.parseObject(json);
        new Thread(() -> {
            if (!jsonObject.getBoolean("error")) {
                String lastId = jsonObject.getJSONObject("body").getString("lastId");
                while (true) {
                    String apiUrl = newApi.replace("@{lastId}", lastId).replace("@{type}", "illust");
                    String jsonStr = OKHttpUtils.get(apiUrl, header, enableProxy);
                    if (!StringUtils.hasText(jsonStr)) {
                        continue;
                    }
                    JSONObject data = JSON.parseObject(jsonStr);
                    if (!data.getBoolean("error")) {
                        lastId = jsonObject.getJSONObject("body").getString("lastId");
                        JSONArray array = jsonObject.getJSONObject("body").getJSONArray("illusts");
                        if (array.size() == 0) {
                            break;
                        }
                        List<String> idList = new ArrayList<>();
                        for (int i = 0; i < array.size(); i++) {
                            String id = array.getJSONObject(i).getString("id");
                            idList.add(id);
                        }
                        if (!CollectionUtils.isEmpty(idList)) {
                            redisTemplate.opsForList().rightPushAll(key, idList);
                        }
                    }
                }
            }
        }).start();

        ExecutorService executorService = Executors.newFixedThreadPool(thread);
        for (int i = 0; i < thread; i++) {
            executorService.execute(() -> {
                while (true) {
                    String id = redisTemplate.opsForList().leftPop(key, 1, TimeUnit.SECONDS);
                    PixivImage pixivImage = getPixivImageByPageId(id);
                    if (Objects.nonNull(pixivImage)) {
                        downloadImage(pixivImage);
                    }
                }
            });
        }
    }

    public void downloadRankImage() {
        String key = "pixivRankTask";
        new Thread(() -> {
            long time = System.currentTimeMillis();
            while (true) {
                time = time - (24 * 60 * 60 * 1000);
                List<String> idList = getHistoryRankListUrl(new Date(time));
                if (idList == null) {
                    break;
                }
                if (idList.size() == 0) {
                    break;
                }
                redisTemplate.opsForList().rightPushAll(key, idList);
            }
        }).start();

        ExecutorService executorService = Executors.newFixedThreadPool(thread);
        for (int i = 0; i < thread; i++) {
            executorService.execute(() -> {
                while (true) {
                    String id = redisTemplate.opsForList().leftPop(key, 1, TimeUnit.SECONDS);
                    if (Objects.nonNull(pixivImageService.findById(id))) {
                        logger.info("{},已存在", id);
                        continue;
                    }
                    PixivImage pixivImage = getPixivImageByPageId(id);
                    if (Objects.nonNull(pixivImage)) {
                        downloadImage(pixivImage);
                        pixivImageService.insert(pixivImage);
                    }
                }
            });
        }
    }

}
