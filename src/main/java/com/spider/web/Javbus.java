package com.spider.web;

import com.spider.entity.ActressesInfo;
import com.spider.entity.AvInfo;
import com.spider.service.ActressesInfoService;
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
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.io.File;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

@Service
public class Javbus extends BaseWeb {

    @Value("${javbus.actressesListTemplate}")
    private String actressesListTemplate;

    @Value("${javbus.actressesDetailsTemplate}")
    private String actressesDetailsTemplate;

    @Value("${javbus.avTemplate}")
    private String avTemplate;

    @Value("${javbus.magnetApi}")
    private String magnetApi;

    @Value("${javbus.uncensoredPage}")
    private String uncensoredPage;

    @Autowired
    private ActressesInfoService actressesInfoService;

    public List<ActressesInfo> getActressesInfoList(String category, int page) {
        String url = actressesListTemplate.replace("@{category}", category).replace("@{page}", String.valueOf(page));
        Document document = JsoupUtil.getDocument(url, enableProxy);
        if (Objects.isNull(document)) {
            return null;
        }
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
                String imgUrl = home+img.attr("src");
                String name = img.attr("title");
                if (!"https://images.javbus.com/actress/nowprinting.gif".equals(imgUrl)) {
                    byte[] imgBytes = OKHttpUtils.getBytes(imgUrl, enableProxy);
                    if (Objects.nonNull(imgBytes)) {
                        String path = savePath + "actresses" + fileSeparator + FileUtils.repairPath(name) + ".jpg";
                        FileUtils.byteToFile(imgBytes, path);
                    }
                    actressesInfo.setJavbusPhoto(imgBytes);
                    actressesInfo.setJavbusPhotoUrl(imgUrl);
                }
                actressesInfo.setName(name);
                actressesInfo.setJavbusUrl(infoUrl);
                actressesInfo.setCreateDate(new Date());
                logger.info("{},信息获取完成", name);
            }
            return actressesInfo;
        }).collect(Collectors.toList());
    }

    public List<AvInfo> getAvInfoUrlByActresses(String actressesUrl) {
        List<AvInfo> list = new CopyOnWriteArrayList<>();
        int page = 1;
        while (true) {
            try {
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
                        String imgSrc = home+img.attr("src");
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
                                //e.printStackTrace();
                            }
                        }
                    }
                    avInfo.setCreateDate(new Date());
                    list.add(avInfo);
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
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
            String imgUrl =home+elements.get(0).attr("href");
            avInfo.setCoverUrl(imgUrl);
            byte[] imgBytes = OKHttpUtils.getBytes(imgUrl, enableProxy);
            if (Objects.nonNull(imgBytes)) {
                String path = savePath + "av" + File.separator + avInfo.getCode().trim() + File.separator + "cover.jpg";
                FileUtils.byteToFile(imgBytes, path);
                avInfo.setCover(imgBytes);
            }
        }
        Elements infos = document.getElementsByClass("info");
        if (infos.size() > 0) {
            Element info = infos.get(0);
            Elements infoList = info.getElementsByTag("p");
            infoList.stream().sequential().forEach(p -> {
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
            List<String> tags = info.getElementsByClass("genre").stream().map(Element::text).collect(Collectors.toList());
            avInfo.setTags(tags);
        }
        Element avatar = document.getElementById("avatar-waterfall");
        if (Objects.nonNull(avatar)) {
            List<String> avatarList = avatar.getElementsByTag("img").stream().map(element -> element.attr("title")).collect(Collectors.toList());
            avInfo.setAvatars(avatarList);
        }
        Element waterfall = document.getElementById("sample-waterfall");
        if (Objects.nonNull(waterfall)) {
            List<String> waterfallList = waterfall.getElementsByClass("sample-box").stream().map(element ->home+element.attr("href")).filter(StringUtils::hasText).collect(Collectors.toList());
            avInfo.setPreviewImageUrlList(waterfallList);
            List<byte[]> byteList = waterfallList.stream().parallel().map(url -> {
                byte[] bytes = OKHttpUtils.getBytes(url, enableProxy);
                if (Objects.nonNull(bytes)) {
                    String path = savePath + "av" + File.separator + avInfo.getCode().trim() + File.separator + DigestUtils.md5DigestAsHex(bytes) + ".jpg";
                    FileUtils.byteToFile(bytes, path);
                }
                return bytes;
            }).collect(Collectors.toList());
            avInfo.setPreviewImageList(byteList);
        }
        avInfo.setMagnetList(getMagnetList(avInfo.getSourceUrl()));
        logger.info("{},{},获取完成", avInfo.getCode(), avInfo.getName());
        return avInfo;
    }

    public List<AvInfo.Magnet> getMagnetList(String url) {
        Document document = JsoupUtil.getDocument(url, enableProxy);
        if (Objects.isNull(document)) {
            return null;
        }
        String scriptString = "";
        Elements scripts = document.getElementsByTag("script");
        for (Element element : scripts) {
            if (element.data().contains("gid")) {
                scriptString = element.data();
                break;
            }
        }
        List<AvInfo.Magnet> magnetList = new CopyOnWriteArrayList<>();
        String gid = String.valueOf(getGid(scriptString));
        if (StringUtils.hasText(gid)) {
            String apiUrl = magnetApi.replace("@{gid}", gid);
            Map<String, String> header = new HashMap<>();
            header.put("referer", home);
            String apihtml = OKHttpUtils.get(apiUrl, header, enableProxy);
            if (Objects.nonNull(apihtml)) {
                apihtml = "<table>" + apihtml + "</table>";
                Document apiDocument = Jsoup.parseBodyFragment(apihtml);
                apiDocument.getElementsByTag("tr").stream().sequential().forEach(tr -> {
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
                            size = Double.parseDouble(sizeStr.replace("MB", "0"));
                        } else if (sizeStr.endsWith("GB")) {
                            size = Double.parseDouble(sizeStr.replace("GB", "0")) * 1024;
                        }
                        magnetInfo.setSize(size);
                        String shareDate = dataList.get(2).text();
                        try {
                            magnetInfo.setShareDate(simpleDateFormat.parse(shareDate));
                        } catch (Exception e) {
                            //e.printStackTrace();
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
                            size = Double.parseDouble(sizeStr.replace("MB", "0"));
                        } else if (sizeStr.endsWith("GB")) {
                            size = Double.parseDouble(sizeStr.replace("GB", "0")) * 1024;
                        }
                        magnetInfo.setSize(size);
                        String shareDate = dataList.get(3).text();
                        try {
                            magnetInfo.setShareDate(simpleDateFormat.parse(shareDate));
                        } catch (Exception e) {
                            //e.printStackTrace();
                        }
                    }
                    magnetList.add(magnetInfo);
                });
            }
        }
        return magnetList.stream().filter(magnet -> StringUtils.hasText(magnet.getMagnet())).collect(Collectors.toList());
    }

    public void saveAvInfoByActresses(String actressesUrl) {
        List<AvInfo> list = getAvInfoUrlByActresses(actressesUrl);
        list.stream().filter(Objects::nonNull).parallel().forEach(avInfo -> {
            if (avInfoService.count("code", avInfo.getCode()) == 0) {
                avInfo = getAvInfo(avInfo);
                if (Objects.nonNull(avInfo)) {
                    avInfoService.insert(avInfo);
                }
            } else {
                logger.info("{},已存在", avInfo.getCode());
            }
        });
    }

    public void saveAvInfoByActressesAll() {
        ForkJoinPool forkJoinPool = new ForkJoinPool(thread);
        forkJoinPool.submit(() -> {
            actressesInfoService.findAll().stream().parallel().forEach(actressesInfo -> {
                logger.info("----------{},开始获取---------", actressesInfo.getName());
                saveAvInfoByActresses(actressesInfo.getJavbusUrl());
                logger.info("----------{},所有获取完成---------", actressesInfo.getName());
            });
            logger.info("----------保存完成-------------");
        });
    }

    public void updateAVMagnetList() {
        List<AvInfo> list = avInfoService.findAll();
        list.stream().parallel().forEach(avInfo -> {
            try {
                logger.info("{},开始获取磁力链接", avInfo.getCode());
                List<AvInfo.Magnet> magnetList = getMagnetList(avInfo.getSourceUrl());
                if (magnetList.size() > 0) {
                    avInfo.setMagnetList(magnetList);
                    avInfoService.updateById(avInfo);
                }
                logger.info("{},获取磁力链接完成", avInfo.getCode());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
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


    public void saveNewAvInfo() {
        int page = 1;
        int endPage = 100;
        while (true) {
            try {
                List<AvInfo> list = new CopyOnWriteArrayList<>();
                String url = uncensoredPage.replace("@{page}", String.valueOf(page));
                Document document = JsoupUtil.getDocument(url, enableProxy);
                if (Objects.isNull(document)) {
                    continue;
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
                                //e.printStackTrace();
                            }
                        }
                    }
                    avInfo.setCreateDate(new Date());
                    list.add(avInfo);
                });
                list.stream().parallel().forEach(avInfo -> {
                    if (CollectionUtils.isEmpty(avInfoService.findBykeyValue("code", avInfo.getCode()))) {
                        AvInfo getAvInfo = getAvInfo(avInfo);
                        if (Objects.nonNull(getAvInfo)) {
                            avInfoService.insert(getAvInfo);
                        }
                    } else {
                        logger.info("{},已存在", avInfo.getCode());
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
            page++;
            if (page > endPage) {
                break;
            }
        }
        logger.info("更新完成");
    }


    public String getGid(String js) {
        return js.split("gid")[1].replace("=", "").split(";")[0].trim();
    }
}
