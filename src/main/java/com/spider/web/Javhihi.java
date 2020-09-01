package com.spider.web;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.spider.entity.Video;
import com.spider.service.VideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.spider.utils.FileUtils;
import com.spider.utils.JsoupUtil;
import com.spider.utils.OKHttpUtils;
import com.spider.utils.download.MultithreadingDownload;

@Component
public class Javhihi extends BaseWeb {

    @Value("${javhihi.home}")
    private String home;

    @Value("${javhihi.movie}")
    private String movie;

    @Value("${javhihi.savePath}")
    private String savePath;

    @Value("${javhihi.thread}")
    private int thread;

    @Value("${javhihi.enableProxy}")
    private Boolean enableProxy;

    @Autowired
    private MultithreadingDownload MultithreadingDownload;

    @Autowired
    private VideoService videoService;

    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public void getMoive(int page) {
        String url = movie.replace("@{page}", String.valueOf(page));
        String json = OKHttpUtils.get(url, enableProxy);
        JSONObject jsonObject = JSON.parseObject(json);
        JSONArray movies = jsonObject.getJSONArray("movies");
        for (int i = 0; i < movies.size(); i++) {
            try {
                JSONObject movie = movies.getJSONObject(i);
                String name = FileUtils.repairPath(movie.getString("name"));
                String movieUrl = home + movie.getString("url");

                if(Objects.nonNull(videoService.findByName(name+".mp4"))){
                    logger.info(name + ",已存在");
                    continue;
                }

                if(Objects.nonNull(videoService.findBySourceUrl(movieUrl))){
                    logger.info(name + ",已存在");
                    continue;
                }
                Document document = JsoupUtil.getDocument(movieUrl,enableProxy);
                Element element = document.getElementsByClass("dropdown-toggle").get(0);
                String href = element.attr("href");
                String[] strArr = href.split("/");
                String key = strArr[strArr.length - 1];
                String api = "https://anime789.com/api/source/" + key;
                String apiJson = OKHttpUtils.post(api, enableProxy);
                JSONObject apiJsonObject = JSON.parseObject(apiJson);
                if (apiJsonObject.getBoolean("success")) {
                    Video video = new Video();

                    video.setSourceUrl(movieUrl);
                    //video.setPubDate(new Date(movie.getString("published")));
                    video.setCategories(movie.getJSONArray("categories").toJavaList(String.class));
                    video.setStarNames(movie.getJSONArray("pornstars").toJavaList(String.class));
                    video.setTags(movie.getJSONArray("tags").toJavaList(String.class));
                    video.setName(name+".mp4");
                    JSONArray jsonArray = apiJsonObject.getJSONArray("data");
                    JSONObject fileJson = jsonArray.getJSONObject(jsonArray.size() - 1);
                    String redirector = fileJson.getString("file");
                    String realUrl = OKHttpUtils.getRedirectUrl(redirector, enableProxy);
                    if(Objects.isNull(realUrl)){
                        logger.info("获取视频地址失败");
                        continue;
                    }
                    String data = simpleDateFormat.format(new Date());
                    String path = savePath + data + "\\" + name + ".mp4";
                    System.out.println(realUrl);
                    Map<String, String> header = new HashMap<>();
                    header.put("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.106 Safari/537.36 Edg/80.0.361.54");
                    header.put("referer", realUrl);
                    header.put("Accept", "*/*");
                    header.put("Cache-Control", "no-cache");
                    // header.put("Host", realUrl);
                    header.put("Accept-Encoding", "gzip, deflate");
                    header.put("Connection", "keep-alive");
                    video.setVideoUrl(realUrl);
                    video.setSavePath(path);
                    MultithreadingDownload.videoDownload(video, header, enableProxy, thread, defaultSegmentSize);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    public void downloadVideo() {
        int i = 166;
        while (true) {
            try {
                getMoive(i);
            } catch (Exception e) {
                e.printStackTrace();
            }
            i--;
        }
    }
}
