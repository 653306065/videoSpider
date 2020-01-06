package com.spider.entity;

import java.util.List;

public class YuotubeChannel {

	private String channelTitle;

	private List<YuotubeVideo> videoList;

	public static class YuotubeVideo {

		private String id;

		private String title;

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}
	}

	public String getChannelTitle() {
		return channelTitle;
	}

	public void setChannelTitle(String channelTitle) {
		this.channelTitle = channelTitle;
	}

	public List<YuotubeVideo> getVideoList() {
		return videoList;
	}

	public void setVideoList(List<YuotubeVideo> videoList) {
		this.videoList = videoList;
	}
	
}
