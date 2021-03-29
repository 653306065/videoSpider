package com.spider.web;

import java.net.Proxy;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.oracle.truffle.js.parser.json.JSONParserUtil;
import com.spider.constant.Constant;
import com.spider.entity.PixivImage;
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
            String json = OKHttpUtils.get(url, header, enableProxy);
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
        return urlList;
    }

    public PixivImage getPixivImageByPageId(String id) {
        if(Objects.isNull(id)){
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
        image.setImageSavePath(image.getImageUrl().stream().sequential().map(url -> {
            byte[] bytes = OKHttpUtils.getBytes(url, header, enableProxy);
            if (Objects.nonNull(bytes)) {
                String path = savePath + fileSeparator + image.getUserId() + fileSeparator + image.getTitle() + fileSeparator + md5.digestHex(bytes) + ".jpg";
                FileUtils.byteToFile(bytes, path);
                logger.info("{},下载完成", path);
                return path;
            }
            return null;
        }).filter(Objects::nonNull).collect(Collectors.toList()));
        logger.info("{},下载完成", image.getTitle());
    }


    public void downloadImage() {
        String key = "pixivTask";
        Map<String, String> header = new HashMap<>();
        header.put("cookie", cookie);
        header.put("user-agent", Constant.user_agent);
        String api = newApi.replace("&lastId=@{lastId}", "").replace("@{type}","illust");
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
                    String apiUrl = newApi.replace("@{lastId}", lastId).replace("@{type}","illust");
                    String jsonStr = OKHttpUtils.get(apiUrl, header, enableProxy);
                    if(!StringUtils.hasText(jsonStr)){
                        continue;
                    }
                    JSONObject data = JSON.parseObject(jsonStr);
                    if (!data.getBoolean("error")) {
                        lastId = jsonObject.getJSONObject("body").getString("lastId");
                        JSONArray array = jsonObject.getJSONObject("body").getJSONArray("illusts");
                        if(array.size()==0){
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
                while (true){
                    String id = redisTemplate.opsForList().leftPop(key, 1, TimeUnit.SECONDS);
                    PixivImage pixivImage = getPixivImageByPageId(id);
                    if (Objects.nonNull(pixivImage)) {
                        downloadImage(pixivImage);
                    }
                }
            });
        }
    }

}
