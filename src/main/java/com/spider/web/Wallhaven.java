package com.spider.web;

import java.net.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.spider.utils.JsoupUtil;
import com.spider.utils.download.ImageDownload;

@Service
public class Wallhaven {

	@Value("${wallhaven.imageApi}")
	private String imageApi;

	@Value("${wallhaven.cookie}")
	private String cookie;

	@Value("${wallhaven.savePath}")
	private String savePath;

	@Autowired
	ImageDownload imageDownload;

	@Autowired
	private Proxy proxy;

	private Logger logger = LoggerFactory.getLogger(Wallhaven.class);

	public List<String> getImageList(String url) {
		List<String> imageList = new ArrayList<String>();
		Map<String, String> header = new HashMap<String, String>();
		header.put("cookie", cookie);
		Document document = JsoupUtil.getDocumentByProxy(url, header);
		Elements elements = document.getElementsByClass("thumb-listing-page").get(0).getElementsByTag("ul").get(0)
				.getElementsByTag("li");
		for (Element element : elements) {
			String id = element.getElementsByTag("figure").get(0).attr("data-wallpaper-id");
			String pngImageUrl = "https://wallpapers.wallhaven.cc/wallpapers/full/wallhaven-" + id + ".png";
			String jpgImageUrl = "https://wallpapers.wallhaven.cc/wallpapers/full/wallhaven-" + id + ".jpg";
			imageList.add(jpgImageUrl);
			imageList.add(pngImageUrl);
		}
		return imageList;
	}

	public void downloadImageList(List<String> list, String path) {
		ExecutorService executorService = Executors.newFixedThreadPool(list.size());
		for (String imageUrl : list) {
			executorService.execute(new Runnable() {
				@Override
				public void run() {
					String name = imageUrl.split("-")[1];
					String save = savePath + path + name;
					imageDownload.downloadFile(imageUrl, null, save, proxy);
				}
			});
		}
		executorService.shutdown();
		while (true) {
			if (executorService.isTerminated()) {
				break;
			}
		}
	}

	public void downloadImage(String categories, String purity, String atleast, String path) {
		int startPage = 1;
		while (true) {
			try {
				String url = imageApi.replace("@{categories}", categories).replace("@{purity}", purity).replace("@{atleast}", atleast).replace("@{page}", String.valueOf(startPage));
				List<String> list = getImageList(url);
				downloadImageList(list, path);
				if (list.size() == 0) {
					break;
				}
				logger.info("第{}页,下载完成", String.valueOf(startPage));
				startPage++;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void downloadNSFWAnimeALL() {
		String categories="010";
		String purity="001";
		String atleast = "";
		String path="//NSFW//Anime//ALL//";
		downloadImage(categories, purity, atleast, path);
	}
	
	public void downloadNSFWPeopleALL() {
		String categories="001";
		String purity="001";
		String atleast = "";
		String path="//NSFW//People//ALL//";
		downloadImage(categories, purity, atleast, path);
	}
	
	public void downloadNSFWGeneralALL() {
		String categories="100";
		String purity="001";
		String atleast = "";
		String path="//NSFW//Genera//ALL//";
		downloadImage(categories, purity, atleast, path);
	}
	
	public void downloadAnime4K() {
		String categories="010";
		String purity="110";
		String atleast = "3840x2160";
		String path="//Anime//4k//";
		downloadImage(categories, purity, atleast, path);
	}
	
	public void downloadAnime1080() {
		String categories="010";
		String purity="110";
		String atleast = "1920x1080";
		String path="//Anime//1080//";
		downloadImage(categories, purity, atleast, path);
	}
	
	public void downloadAnime2k() {
		String categories="010";
		String purity="110";
		String atleast = "2560x1440";
		String path="//Anime//2k//";
		downloadImage(categories, purity, atleast, path);
	}
}
