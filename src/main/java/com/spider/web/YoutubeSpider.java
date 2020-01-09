package com.spider.web;

import java.io.File;
import java.io.IOException;
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
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.ChannelListResponse;
import com.google.api.services.youtube.model.Playlist;
import com.google.api.services.youtube.model.PlaylistItem;
import com.google.api.services.youtube.model.PlaylistItemListResponse;
import com.google.api.services.youtube.model.PlaylistListResponse;
import com.spider.utils.FFmpegUtil;
import com.spider.utils.FileUtils;
import com.spider.utils.JsoupUtil;
import com.spider.utils.OKHttpUtils;

@Component
public class YoutubeSpider {

	private Logger logger = LoggerFactory.getLogger(YoutubeSpider.class);

	@Value("${youtube.savePath}")
	private String savePath;

	@Value("${youtube.home}")
	private String home;

	@Value("${youtube.thread}")
	private Integer thread;

	@Value("${youtube.api}")
	private String api;

	@Value("${youtube.googleApi.key}")
	private String key;

	@Autowired
	private Proxy proxy;

	@Autowired
	MultithreadingDownload multithreadingDownload;

	@Autowired
	YouTube youTube;

	public String getApiToken() {
		Document document = JsoupUtil.getDocumentByProxy(home);
		String csrf_token = document.getElementById("csrf_token").attr("value");
		return csrf_token;
	}

	/**
	 * 获取用户的播放列表
	 * 
	 * @param channelId
	 * @return
	 */
	public List<Playlist> getPlayList(String channelId) {
		List<Playlist> list = new ArrayList<>();
		try {
			PlaylistListResponse playlistItemList = youTube.playlists().list("snippet").setMaxResults(50L)
					.setChannelId(channelId).setKey(key).execute();
			list.addAll(playlistItemList.getItems());
			if (playlistItemList.getNextPageToken() != null) {
				String pageToken = playlistItemList.getNextPageToken();
				while (true) {
					PlaylistListResponse response  = youTube.playlists().list("snippet").setPageToken(pageToken).setMaxResults(50L)
							.setChannelId(channelId).setKey(key).execute();
					pageToken = response.getNextPageToken();
					list.addAll(response.getItems());
					if (pageToken == null) {
						break;
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return list;
	}

	/**
	 * 获取播放列表的视频
	 * 
	 * @param channelId
	 * @return
	 */
	public void downloadPlayListItems(Playlist playlist) {
		try {
			List<PlaylistItem> list = new ArrayList<PlaylistItem>();
			PlaylistItemListResponse playlistItemListResponse = youTube.playlistItems().list("snippet").setKey(key)
					.setPlaylistId(playlist.getId()).setMaxResults(50L).execute();
			list.addAll(playlistItemListResponse.getItems());
			if (playlistItemListResponse.getNextPageToken() != null) {
				String pageToken = playlistItemListResponse.getNextPageToken();
				while (true) {
					playlistItemListResponse = youTube.playlistItems().list("snippet").setPageToken(pageToken)
							.setKey(key).setPlaylistId(playlist.getId()).setMaxResults(50L).execute();
					pageToken = playlistItemListResponse.getNextPageToken();
					list.addAll(playlistItemListResponse.getItems());
					if (pageToken == null) {
						break;
					}
				}
			}
			String playListTitle = playlist.getSnippet().getTitle();
			for (PlaylistItem item : list) {
				logger.info("PlaylistItem:{}", JSON.toJSONString(item));
				String videoId = item.getSnippet().getResourceId().getVideoId();
				String channelTitle = item.getSnippet().getChannelTitle();
				Map<String, String> urlMap = getVideoUrlList(videoId);
				String title = item.getSnippet().getTitle();
				String videoUrl = urlMap.get("videoUrl");
				String audioUrl = urlMap.get("audioUrl");
				String videoName = urlMap.get("videoName").replaceAll(" ", "");
				String audioName = urlMap.get("audioName").replaceAll(" ", "");
				logger.info("vidoeTitle:{},开始下载", title);
				String videoPath = this.savePath +FileUtils.repairPath(channelTitle) + "\\" +FileUtils.repairPath(playListTitle)+ FileUtils.repairPath(playListTitle) + "\\" +FileUtils.repairPath(title) +FileUtils.repairPath(videoName);
				String audioPath = this.savePath +FileUtils.repairPath(channelTitle) + "\\" +FileUtils.repairPath(playListTitle)+ FileUtils.repairPath(playListTitle) + "\\" +FileUtils.repairPath(title) +FileUtils.repairPath(audioName);
				String targetPath = this.savePath +FileUtils.repairPath(channelTitle) + "\\" +FileUtils.repairPath(playListTitle)+ FileUtils.repairPath(playListTitle) + "\\" +FileUtils.repairPath(title) +".mp4";
				multithreadingDownload.fileDownload(videoUrl, videoPath, null, proxy, thread);
				logger.info("title:{},视频下载完成", title);
				multithreadingDownload.fileDownload(audioUrl, audioPath, null, proxy, thread);
				logger.info("title:{},音频下载完成", title);
				if (new File(videoPath).exists() && new File(audioPath).exists()) {
					FFmpegUtil.audioVideoSynthesis(videoPath, audioPath, targetPath);
					File targetFile = new File(targetPath);
					targetFile.renameTo(new File(
							targetFile.getParentFile().getPath() + "\\" + targetFile.getName().replaceAll("_", "")));
					logger.info("title:{},音视频合并完成", title);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void downloadByChannelId(String ChannelId) {
		List<Playlist> list = getPlayList(ChannelId);
		for (Playlist Playlist : list) {
			logger.info("Playlist:{}", JSON.toJSONString(list));
			downloadPlayListItems(Playlist);
		}
	}

	public String getUserChannelId(String user) {
		try {
			ChannelListResponse channelListResponse = youTube.channels().list("snippet").setForUsername(user)
					.setKey(key).execute();
			System.out.println(JSON.toJSONString(channelListResponse));
			if (channelListResponse.getItems().size() > 0) {
				return channelListResponse.getItems().get(0).getId();
			}
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
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
