package com.spider.web;

import com.spider.entity.ActressesInfo;
import com.spider.entity.AvInfo;
import com.spider.service.ActressesInfoService;
import com.spider.service.AvInfoService;
import com.spider.utils.FileUtils;
import com.spider.utils.JsoupUtil;
import com.spider.utils.OKHttpUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Service
public class Javbus extends BaseWeb {

    @Value("${javbus.home}")
    private String home;

    @Value("${javbus.enableProxy}")
    private Boolean enableProxy;

    @Value("${javbus.thread}")
    private Integer thread;

    @Value("${javbus.actressesListTemplate}")
    private String actressesListTemplate;

    @Value("${javbus.actressesDetailsTemplate}")
    private String actressesDetailsTemplate;

    @Value("${javbus.avTemplate}")
    private String avTemplate;

    @Value("${javbus.savePath}")
    private String savePath;

    @Value("${javbus.magnetApi}")
    private String magnetApi;

    @Autowired
    private ActressesInfoService actressesInfoService;

    @Autowired
    private AvInfoService avInfoService;

    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public List<ActressesInfo> getActressesInfoList(String category, int page) {
        String url = actressesListTemplate.replace("@{category}", category).replace("@{page}", String.valueOf(page));
        Document document = JsoupUtil.getDocument(url, enableProxy);
        Elements elements = document.getElementsByClass("avatar-box");
        return elements.stream().parallel().map(element -> {
            String infoUrl = element.attr("href");
            ActressesInfo actressesInfo = new ActressesInfo();
            if ("uncensored".equals(category)) {
                actressesInfo.setUncensored(true);
            }
            Elements imgs = element.getElementsByTag("img");
            if (imgs.size() != 0) {
                Element img = imgs.get(0);
                String imgUrl = img.attr("src");
                String name = img.attr("title");
                if (!"https://images.javbus.com/actress/nowprinting.gif".equals(imgUrl)) {
                    byte[] imgBytes = OKHttpUtils.getBytes(imgUrl, enableProxy);
                    if (Objects.nonNull(imgBytes)) {
                        String path = savePath + "actresses" + File.separator + FileUtils.repairPath(name) + ".jpg";
                        FileUtils.byteToFile(imgBytes, path);
                    }
                    actressesInfo.setJavbusPhoto(imgBytes);
                }
                actressesInfo.setJavbusPhotoUrl(imgUrl);
                actressesInfo.setName(name);
                actressesInfo.setJavbusUrl(infoUrl);
                logger.info("{},信息获取完成", name);
            }
            return actressesInfo;
        }).collect(Collectors.toList());
    }

    public List<AvInfo> getAvInfoUrlByActresses(String actressesUrl) {
        List<AvInfo> list = new CopyOnWriteArrayList<>();
        int page = 1;
        while (true) {
            String url = actressesUrl + "/" + page;
            Document document = JsoupUtil.getDocument(url, enableProxy);
            if (Objects.isNull(document)) {
                break;
            }
            Elements elements = document.getElementsByClass("movie-box");
            if (Objects.isNull(elements) || elements.size() == 0) {
                break;
            }
            elements.stream().parallel().forEach(element -> {
                AvInfo avInfo = new AvInfo();
                String avUrl = element.attr("href");
                avInfo.setSourceUrl(avUrl);
                Elements imgs = element.getElementsByTag("img");
                if (Objects.nonNull(imgs) && imgs.size() != 0) {
                    Element img = imgs.get(0);
                    String imgSrc = img.attr("src");
                    avInfo.setThumbUrl(imgSrc);
                    String title = img.attr("title");
                    avInfo.setName(title);
                    byte[] imgBytes = OKHttpUtils.getBytes(imgSrc, enableProxy);
                    if (Objects.nonNull(imgBytes)) {
                        avInfo.setThumb(imgBytes);
                    }
                }
                Elements dates = element.getElementsByTag("date");
                if (dates.size() == 2) {
                    String code = dates.get(0).text();
                    avInfo.setCode(code);
                    String date = dates.get(1).text();
                    if (Objects.nonNull(date)) {
                        try {
                            avInfo.setPublishDate(simpleDateFormat.parse(date));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                list.add(avInfo);
            });
            page++;
        }
        return list;
    }

    public AvInfo getAvInfo(AvInfo avInfo) {
        logger.info("{},{},开始获取", avInfo.getCode(), avInfo.getName());
        String sourceUrl = avInfo.getSourceUrl();
        Document document = JsoupUtil.getDocument(sourceUrl, enableProxy);
        if (Objects.isNull(document)) {
            return null;
        }
        Elements elements = document.getElementsByClass("bigImage");
        if (elements.size() > 0) {
            String imgUrl = elements.get(0).attr("href");
            avInfo.setCoverUrl(imgUrl);
            byte[] imgBytes = OKHttpUtils.getBytes(imgUrl, enableProxy);
            if (Objects.nonNull(imgBytes)) {
                String path = savePath + "av" + File.separator + avInfo.getCode().trim() + File.separator + "cover" + File.separator + "cover.jpg";
                FileUtils.byteToFile(imgBytes, path);
                avInfo.setCover(imgBytes);
            }
        }
        Elements infos = document.getElementsByClass("info");
        if (infos.size() > 0) {
            Element info = infos.get(0);
            Elements infoList = info.getElementsByTag("p");
            infoList.stream().parallel().forEach(p -> {
                String text = p.text();
                String[] arr = text.split(":");
                if (arr.length > 1) {
                    String key = arr[0];
                    String value = arr[1];
                    if ("識別碼".equals(key)) {
                        avInfo.setCode(value.trim());
                    } else if ("發行日期".equals("key")) {
                        try {
                            avInfo.setPublishDate(simpleDateFormat.parse(value));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else if ("長度".equals("key")) {
                        value = value.replace("分鐘", "");
                        avInfo.setMinute(Integer.valueOf(value));
                    } else if ("製作商".equals("key")) {
                        avInfo.setMaker(value);
                    } else if ("系列".equals(key)) {
                        avInfo.setSeries(value);
                    }
                }
            });
            List<String> tags = info.getElementsByClass("genre").stream().parallel().map(element -> {
                return element.text();
            }).collect(Collectors.toList());
            avInfo.setTags(tags);
        }
        Element avatar = document.getElementById("avatar-waterfall");
        if (Objects.nonNull(avatar)) {
            List<String> avatarList = avatar.getElementsByTag("img").stream().map(element -> {
                return element.attr("title");
            }).collect(Collectors.toList());
            avInfo.setAvatars(avatarList);
        }
        Element waterfall = document.getElementById("sample-waterfall");
        if (Objects.nonNull(waterfall)) {
            List<String> waterfallList = waterfall.getElementsByTag("img").stream().map(element -> {
                return element.attr("src");
            }).collect(Collectors.toList());
            avInfo.setPreviewImageUrlList(waterfallList);
            List<byte[]> byteList = waterfallList.stream().parallel().map(url -> {
                byte[] bytes = OKHttpUtils.getBytes(url, enableProxy);
                if (Objects.nonNull(bytes)) {
                    String path = savePath + "av" + File.separator + avInfo.getCode().trim() + File.separator + System.currentTimeMillis() + ".jpg";
                    FileUtils.byteToFile(bytes, path);
                }
                return bytes;
            }).collect(Collectors.toList());
            avInfo.setPreviewImageList(byteList);
        }

        String scriptString = "";
        Elements scripts = document.getElementsByTag("script");
        for (Element element : scripts) {
            if (element.data().contains("gid")) {
                scriptString = element.data();
                break;
            }
        }
        String gid = String.valueOf(getGid(scriptString));
        if (!StringUtils.isEmpty(gid)) {
            String apiUrl = magnetApi.replace("@{gid}", gid);
            Map<String, String> header = new HashMap<>();
            header.put("referer", home);
            String apihtml = OKHttpUtils.get(apiUrl, header, enableProxy);
            if (Objects.nonNull(apihtml)) {
                apihtml = "<table>" + apihtml + "</table>";
                Document apiDocument = Jsoup.parseBodyFragment(apihtml);
                List<AvInfo.Magnet> magnetList = new CopyOnWriteArrayList<>();
                apiDocument.getElementsByTag("tr").stream().forEach(tr -> {
                    AvInfo.Magnet magnetInfo = new AvInfo.Magnet();
                    Elements dataList = tr.getElementsByTag("a");
                    if (dataList.size() == 3) {
                        String magnet = dataList.get(0).attr("href");
                        magnetInfo.setMagnet(magnet);
                        String name = dataList.get(0).text();
                        magnetInfo.setName(name);
                        String sizeStr = dataList.get(1).text();
                        magnetInfo.setSizeStr(sizeStr);
                        double size = 0;
                        if (sizeStr.endsWith("MB")) {
                            size = Double.valueOf(sizeStr.replace("MB", "0"));
                        } else if (sizeStr.endsWith("GB")) {
                            size = Double.valueOf(sizeStr.replace("GB", "0")) * 1024;
                        }
                        magnetInfo.setSize(size);
                        String shareDate = dataList.get(2).text();
                        try {
                            magnetInfo.setShareDate(simpleDateFormat.parse(shareDate));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else if (dataList.size() == 4) {
                        String magnet = dataList.get(0).attr("href");
                        magnetInfo.setMagnet(magnet);
                        String name = dataList.get(0).text();
                        magnetInfo.setName(name);
                        String sizeStr = dataList.get(2).text();
                        magnetInfo.setSizeStr(sizeStr);
                        double size = 0;
                        if (sizeStr.endsWith("MB")) {
                            size = Double.valueOf(sizeStr.replace("MB", "0"));
                        } else if (sizeStr.endsWith("GB")) {
                            size = Double.valueOf(sizeStr.replace("GB", "0")) * 1024;
                        }
                        magnetInfo.setSize(size);
                        String shareDate = dataList.get(3).text();
                        try {
                            magnetInfo.setShareDate(simpleDateFormat.parse(shareDate));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    magnetList.add(magnetInfo);
                });
                avInfo.setMagnetList(magnetList);
            }
        }
        logger.info("{},{},获取完成", avInfo.getCode(), avInfo.getName());
        return avInfo;
    }

    public void saveAvInfoByActresses(String actressesUrl) {
        List<AvInfo> list = getAvInfoUrlByActresses(actressesUrl);
        list.stream().parallel().forEach(avInfo -> {
            if (Objects.nonNull(avInfo) && avInfoService.count("code", avInfo.getCode()) == 0) {
                avInfo = getAvInfo(avInfo);
                if (Objects.nonNull(avInfo)) {
                    avInfoService.insert(avInfo);
                }
            }
        });
    }

    public void saveAvInfoByActressesAll() {
        actressesInfoService.findAll().stream().forEach(actressesInfo -> {
            logger.info("----------{},开始获取---------", actressesInfo.getName());
            saveAvInfoByActresses(actressesInfo.getJavbusUrl());
            logger.info("----------{},所有获取完成---------", actressesInfo.getName());
        });
        logger.info("----------保存完成-------------");
    }

    public void saveAllUncensoredActressesInfo() {
        int page = 1;
        while (true) {
            try {
                List<ActressesInfo> list = getActressesInfoList("uncensored", page);
                if (list == null || list.size() == 0) {
                    break;
                }
                list.stream().filter(info -> Objects.nonNull(info.getName())).parallel().forEach(info -> {
                    if (actressesInfoService.count("name", info.getName()) == 0 && actressesInfoService.count("javbusUrl", info.getJavbusUrl()) == 0) {
                        actressesInfoService.insert(info);
                        logger.info("{},保存完成", info.getName());
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
            page++;
        }
        logger.info("信息保存完成");
    }


    public String getGid(String js) {
        return js.split("gid")[1].replace("=", "").split(";")[0].trim();
    }

}
