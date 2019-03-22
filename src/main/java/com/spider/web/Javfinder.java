package com.spider.web;

import java.net.Proxy;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.spider.utils.OKHttpUtils;
import com.spider.utils.download.MultithreadingDownload;

@Service
public class Javfinder {

	@Value("${javfinder.home}")
	private String home;

	@Value("${javfinder.category.Uncensored}")
	private String Uncensored;

	@Autowired
	private Proxy proxy;

	@Value("${javfinder.savePath}")
	private String savePath;

	@Autowired
	private MultithreadingDownload multithreadingDownload;

	@Value("${javfinder.thread}")
	private int thread;

	public List<String> getVideoInfoUrlList(String url) {
		List<String> list = new ArrayList<String>();
		String html = OKHttpUtils.get(url, proxy);
		Document document = Jsoup.parse(html);
		Elements elements = document.getElementsByClass("main-thumb");
		for (Element element : elements) {
			String videoUrl = home + element.attr("href");
			list.add(videoUrl);
		}
		return list;
	}

	public Map<String, String> getVideoUrl(String url) {
		Map<String, String> info = new HashMap<String, String>();
		String html = OKHttpUtils.get(url, proxy);
		Document document = Jsoup.parse(html);
		String name = document.getElementsByClass("wrap-meta").get(0).getElementsByTag("h1").text();
		String iframeUrl = home + document.getElementById("avcms_player").attr("src");
		String iframeHtml = OKHttpUtils.get(iframeUrl, proxy);
		Document iframeDocument = Jsoup.parse(iframeHtml);
		String dataVideo = iframeDocument.getElementsByClass("active").get(0).attr("data-video");
		String code = dataVideo.split("/")[dataVideo.split("/").length - 1];
		String apiUrl = "https://embed.media/api/source/" + code;
		String json = OKHttpUtils.post(apiUrl, proxy);
		JSONObject jsonObject = JSON.parseObject(json);
		if (jsonObject.getBooleanValue("success")) {
			JSONArray jsonArray = jsonObject.getJSONArray("data");
			String file = jsonArray.getJSONObject(jsonArray.size() - 1).getString("file");
			info.put("videoUrl",OKHttpUtils.getRedirectUrl(file, proxy));
		}
		info.put("name", name);
		return info;
	}

	public void downloadUncensored() {
		int page = 1;
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		while (true) {
			String url = Uncensored.replace("@{page}", String.valueOf(page));
			List<String> list = this.getVideoInfoUrlList(url);
			for (String str : list) {
				Map<String, String> map = this.getVideoUrl(str);
				String fileUrl = map.get("videoUrl");
				String date = simpleDateFormat.format(new Date());
				String path = savePath + "\\" + date + "\\" + map.get("name") + ".mp4";
				multithreadingDownload.fileDownload(fileUrl, path, null, proxy, thread);
			}
		}
	}
}
