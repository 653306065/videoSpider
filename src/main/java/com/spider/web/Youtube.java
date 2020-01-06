package com.spider.web;

import java.io.File;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.spider.utils.download.MultithreadingDownload;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.spider.entity.YuotubeChannel;
import com.spider.entity.YuotubeChannel.YuotubeVideo;
import com.spider.utils.FFmpegUtil;
import com.spider.utils.JsoupUtil;
import com.spider.utils.OKHttpUtils;

@Component
public class Youtube {

	private Logger logger = LoggerFactory.getLogger(Wallhaven.class);

	@Value("${youtube.savePath}")
	private String savePath;

	@Value("${youtube.home}")
	private String home;

	@Value("${youtube.thread}")
	private Integer thread;

	@Value("${youtube.api}")
	private String api;

	@Autowired
	private Proxy proxy;

	@Autowired
	MultithreadingDownload multithreadingDownload;

	@Value("${youtube.googleApi.playlists}")
	private String playlistsApi;// 获取视频播放列表

	@Value("${youtube.googleApi.playlistItems}")
	private String playlistItems;// 获取视频列表中的视频项

	@Value("${youtube.googleApi.search}")
	private String search;// 获取视频列表中的视频项

	public String getApiToken() {
		Document document = JsoupUtil.getDocumentByProxy(home);
		String csrf_token = document.getElementById("csrf_token").attr("value");
		return csrf_token;
	}

	/**
	 * 获取用户的播放列表
	 * @param channelId
	 * @return
	 */
	public String getPlayList(String channelId) {
		String realUrl = playlistsApi.replace("@{channelId}", channelId);
		String json = OKHttpUtils.get(realUrl, proxy);
		return json;
	}

	/**
	 * 获取播放列表的视频
	 * @param channelId
	 * @return
	 */
	public List<JSONObject> getPlayListItems(String json) {
		JSONObject jsonObject = JSON.parseObject(json);
		JSONArray items = jsonObject.getJSONArray("items");
		String channelTitle = "";
		List<JSONObject> playlist = new ArrayList<JSONObject>();
		for (int i = 0; i < items.size(); i++) {
			JSONObject item = items.getJSONObject(i);
			String id = item.getString("id");
			String title = item.getJSONObject("snippet").getString("title");
			channelTitle = item.getJSONObject("snippet").getString("channelTitle");
			String playlistUrl = playlistItems.replace("@{playlistId}", id);
			String playlistJson = OKHttpUtils.get(playlistUrl, proxy);
			JSONObject playlistjJsonObject = JSON.parseObject(playlistJson);
			JSONArray playlistItems = playlistjJsonObject.getJSONArray("items");
			for (int index = 0; index < playlistItems.size(); index++) {
				String videoId = playlistItems.getJSONObject(index).getJSONObject("snippet").getJSONObject("resourceId").getString("videoId");
				downloadVideo(videoId);
			}
			playlist.add(playlistjJsonObject);
		}
		return playlist;
	}

	/**
	 * 谷歌搜索api
	 * @param channelId
	 * @return
	 */
	public YuotubeChannel searchVideoList(String channelId) {
		String realUrl = search.replace("@{channelId}", channelId);
		String json = OKHttpUtils.get(realUrl, proxy);
		JSONObject jsonObject = JSON.parseObject(json);
		YuotubeChannel yuotubeChannel = new YuotubeChannel();
		List<YuotubeVideo> list = new ArrayList<YuotubeVideo>();
		JSONArray items = jsonObject.getJSONArray("items");
		for (int i = 0; i < items.size(); i++) {
			JSONObject item = items.getJSONObject(i);
			String videoId = item.getJSONObject("id").getString("videoId");
			String title = item.getJSONObject("snippet").getString("title");
			String channelTitle = item.getJSONObject("snippet").getString("channelTitle");
			yuotubeChannel.setChannelTitle(channelTitle);
			YuotubeVideo yuotubeVideo = new YuotubeVideo();
			yuotubeVideo.setId(videoId);
			yuotubeVideo.setTitle(title);
			list.add(yuotubeVideo);
		}

		if (jsonObject.containsKey("nextPageToken")) {
			String nextPageToken = jsonObject.getString("nextPageToken");
			while (true) {
				String data = OKHttpUtils.get(realUrl + "&pageToken=" + nextPageToken, proxy);
				JSONObject dataJson = JSON.parseObject(data);
				items = dataJson.getJSONArray("items");
				for (int i = 0; i < items.size(); i++) {
					JSONObject item = items.getJSONObject(i);
					String videoId = item.getJSONObject("id").getString("videoId");
					String title = item.getJSONObject("snippet").getString("title");
					String channelTitle = item.getJSONObject("snippet").getString("channelTitle");
					yuotubeChannel.setChannelTitle(channelTitle);
					YuotubeVideo yuotubeVideo = new YuotubeVideo();
					yuotubeVideo.setId(videoId);
					yuotubeVideo.setTitle(title);
					list.add(yuotubeVideo);
				}
				if (!dataJson.containsKey("nextPageToken")) {
					break;
				} else {
					nextPageToken = dataJson.getString("nextPageToken");
				}
			}
		}
		yuotubeChannel.setVideoList(list);
		return yuotubeChannel;
	}

	public Map<String, String> getVideoUrlList(String url) {
		Map<String, String> params = new HashMap<String, String>();
		params.put("url", url);
		params.put("csrf_token", getApiToken());
		params.put("proxy", "Random");
		String html = OKHttpUtils.postFormData(api, params, proxy);
		Document document = Jsoup.parse(html);
		Element videos_modal = document.getElementById("videos_modal");
		Element tbody = videos_modal.getElementsByTag("tbody").get(0);
		Elements trs = tbody.getElementsByTag("tr");
		String audioUrl = "";
		String videoUrl = "";
		String title = document.getElementsByTag("h3").get(0).ownText();
		List<Float> sizeList = new ArrayList<Float>();
		Map<Float, String> sizeUrlMap = new HashMap<Float, String>();
		Map<String, String> result = new HashMap<String, String>();
		Map<Float, String> sizeNameMap = new HashMap<Float, String>();
		for (Element tr : trs) {
			String name = tr.getElementsByTag("td").get(0).ownText();
			String size = tr.getElementsByTag("td").get(1).ownText();
			String fileUrl = tr.getElementsByTag("td").get(2).getElementsByTag("a").get(0).attr("href");
			if (name.indexOf("audio") != -1) {
				result.put("audioName", name);
				audioUrl = fileUrl;
			} else {
				float sizeF = 0;
				if (size.endsWith("MB")) {
					String mb = size.replace("MB", "");
					sizeF = Float.valueOf(mb);
				} else if (size.endsWith("GB")) {
					String gb = size.replace("GB", "");
					sizeF = Float.valueOf(gb) * 1024;
				}
				sizeNameMap.put(sizeF, name);
				sizeList.add(sizeF);
				sizeUrlMap.put(sizeF, fileUrl);
			}
		}
		Collections.sort(sizeList);
		float max = sizeList.get(sizeList.size() - 1);
		videoUrl = sizeUrlMap.get(max);
		result.put("audioUrl", audioUrl);
		result.put("videoUrl", videoUrl);
		result.put("title", title);
		result.put("videoName", sizeNameMap.get(max));
		System.out.println("title:" + title);
		System.out.println("audioUrl:" + audioUrl);
		System.out.println("videoUrl:" + videoUrl);
		return result;
	}

	public void downloadChannel(String channelId) {
		YuotubeChannel yuotubeChannel = searchVideoList(channelId);
		String ChannelTitle = yuotubeChannel.getChannelTitle();
		for (YuotubeVideo YuotubeVideo : yuotubeChannel.getVideoList()) {
			String title = YuotubeVideo.getTitle();
			logger.info("title:{},开始下载",title);
			Map<String, String> urlMap = getVideoUrlList(YuotubeVideo.getId());
			String videoUrl = urlMap.get("videoUrl");
			String audioUrl = urlMap.get("audioUrl");
			String videoName = urlMap.get("videoName").replaceAll(" ", "");
			String audioName = urlMap.get("audioName").replaceAll(" ", "");
			String videoPath = (this.savePath  + ChannelTitle + "\\" + title + videoName).replaceAll(" ", "_").replaceAll("\\|", "").replaceAll(";", "").replaceAll("&", "");
			String audioPath = (this.savePath  + ChannelTitle + "\\" + title + audioName).replaceAll(" ", "_").replaceAll("\\|", "").replaceAll(";", "").replaceAll("&", "");
			String targetPath = (this.savePath + ChannelTitle + "\\" + title + ".mp4").replaceAll(" ", "_").replaceAll("\\|", "").replaceAll(";", "").replaceAll("&", "");
			multithreadingDownload.fileDownload(videoUrl, videoPath, null, proxy, thread);
			logger.info("title:{},视频下载完成",title);
			multithreadingDownload.fileDownload(audioUrl, audioPath, null, proxy, thread);
			logger.info("title:{},音频下载完成",title);
			if (new File(videoPath).exists() && new File(audioPath).exists()) {
				FFmpegUtil.audioVideoSynthesis(videoPath, audioPath, targetPath);
				logger.info("title:{},音视频合并完成",title);
			}
		}
	}

	public void downloadVideo(String url) {
		Map<String, String> urlMap = getVideoUrlList(url);
		String title = urlMap.get("title");
		String videoUrl = urlMap.get("videoUrl");
		String audioUrl = urlMap.get("audioUrl");
		String videoName = urlMap.get("videoName").replaceAll(" ", "");
		String audioName = urlMap.get("audioName").replaceAll(" ", "");
		String videoPath = (this.savePath + "\\" + title + videoName).replaceAll(" ", "_");
		String audioPath = (this.savePath + "\\" + title + audioName).replaceAll(" ", "_");
		String targetPath = (this.savePath + "\\" + title + ".mp4").replaceAll(" ", "_");
		multithreadingDownload.fileDownload(videoUrl, videoPath, null, proxy, thread);
		multithreadingDownload.fileDownload(audioUrl, audioPath, null, proxy, thread);
		if (new File(videoPath).exists() && new File(audioPath).exists()) {
			FFmpegUtil.audioVideoSynthesis(videoPath, audioPath, targetPath);
		}
	}

}
