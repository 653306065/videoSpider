package com.spider.web;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.alibaba.fastjson.JSON;
import com.spider.entity.By114BT;
import com.spider.utils.FileUtils;
import com.spider.utils.JsoupUtil;
import com.spider.utils.OKHttpUtils;
import com.spider.service.By114BTService;

@Service
public class By114 {

	@Value("${by114.template}")
	private String template;

	@Value("${by114.home}")
	private String home;

	@Value("${by114.savePath}")
	private String savePath;
	
	@Autowired
	private By114BTService by114BTService;

	public List<By114BT> getBTInfo(String type, String page) {
		String url = template.replace("@{type}", type).replace("@{page}", page);
		Document document = JsoupUtil.getDocument(url);
		Element element = document.getElementById("threadlisttableid");
		Elements tbodys = element.getElementsByTag("tbody");
		List<By114BT> list = new ArrayList<By114BT>();
		for (Element tb : tbodys) {
			String id = tb.attr("id");
			if (id.startsWith("normalthread")) {
				By114BT by114BT = new By114BT();
				by114BT.setPageHtml(tb.toString());
				String infoUrl = home + tb.getElementsByClass("icn").get(0).getElementsByTag("a").get(0).attr("href");
				Element name = tb.getElementsByClass("new").get(0).getElementsByClass("xst").get(0);
				if (name != null) {
					by114BT.setUrlName(name.text());
				}
				if ("52".equals(type)) {
					by114BT.setType("52");
				}
				String contentPublishDate = tb.getElementsByClass("by").get(1).getElementsByTag("span").attr("title");
				by114BT.setUrl(infoUrl);
				by114BT.setContentPublishDate(contentPublishDate);
				list.add(by114BT);
			}
		}
		return list;
	}

	public void saveBTInfo(By114BT bt) {
		if(!Objects.isNull( by114BTService.findByUrl(bt.getUrl()))) {
			return;
		}
		Document document = JsoupUtil.getDocument(bt.getUrl());
		Element contentHtml = document.getElementsByClass("t_fsz").get(0);
		Element tf = contentHtml.getElementsByClass("t_f").get(0);
		String title = document.getElementById("thread_subject").text();
		bt.setTitle(title);
		bt.setContentHtml(tf.toString());
		// 内容解析
		if (tf.getElementsByTag("font").size() < 3) {
			bt.setContent(tf.text());
		} else {
			Map<String, String> contentMap = new HashMap<String, String>();
			for (Element element : tf.getElementsByTag("font")) {
				if (element.hasText()) {
					String text = element.text();
					if (text.indexOf("：") != -1 && text.split("：").length == 2) {
						String key = text.split("：")[0];
						String value = text.split("：")[1];
						contentMap.put(key, value);
					}
				}
			}
			for (Map.Entry<String, String> entry : contentMap.entrySet()) {
				if ("【影片名称】".equals(entry.getKey())) {
					bt.setVideoName(entry.getValue());
				}
				if ("【是否有码】".equals(entry.getKey())) {
					bt.setMosaic(entry.getValue());
				}
				if ("【特 徵 碼】".equals(entry.getKey())) {
					bt.setMagnet(entry.getValue());
				}
				if ("【影片格式】".equals(entry.getKey())) {
					bt.setVideoFormat(entry.getValue());
				}
				if ("【影片大小】".equals(entry.getKey())) {
					bt.setVideoSize(entry.getValue());
				}
				if ("【女 优 】".equals(entry.getKey())) {
					bt.setActorName(entry.getValue());
				}
				if ("【作种期限】".equals(entry.getKey())) {
					bt.setBtDeadline(entry.getValue());
				}
			}
		}
		// 图片下载
		List<byte[]> imgList = new ArrayList<byte[]>();
		Elements elements = tf.getElementsByTag("img");
		int index = 0;
		List<String> imgPath=new ArrayList<String>();
		List<String> imgurl=new ArrayList<String>();
		for (Element img : elements) {
			String url = img.attr("src");
			byte[] bytes = OKHttpUtils.getBytes(url);
			String path = savePath + "img" + File.separator + title + "_" + index + ".jpg";
			if (bt.getMagnet() != null) {
				path = savePath + "img" + File.separator + bt.getMagnet() + "_" + index + ".jpg";
			}
			FileUtils.byteToFile(bytes, path);
			imgList.add(bytes);
			imgPath.add(path);
			imgurl.add(url);
			index++;
		}
		bt.setImages(imgList);
		bt.setImagesPath(imgPath);
		bt.setImagesUrl(imgurl);
		
		Elements element= document.getElementsByClass("attnm");
		if(element!=null&&element.size()>0) {
			Element a=element.get(0).getElementsByTag("a").get(0);
			String url=home+a.attr("href");
			String name=a.text();
			byte[] bytes= OKHttpUtils.getBytes(url);
			String path=savePath+"torrent"+File.separator+name;
			FileUtils.byteToFile(bytes, path);
			bt.setTorrent(bytes);
			bt.setTorrentName(name);
			bt.setTorrentPath(path);
			bt.setTorrentUrl(url);
		}
		by114BTService.save(bt);
	}
	
	public void  downloadBt() {
		int i=1;
		while(true) {
		    try {
		    	List<By114BT> list=getBTInfo("52", String.valueOf(i));
				for(By114BT bt:list) {
					saveBTInfo(bt);
				}
				if(list.size()==0) {
					break;
				}
				i++;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
