package com.spider.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;

import ws.schild.jave.MultimediaInfo;
import ws.schild.jave.MultimediaObject;
import ws.schild.jave.VideoInfo;

@Component
public class FFmpegUtil {

	public static String FFmpegPath = "C:\\ffmpeg\\bin\\";

	@Value("${FFmpegPath}")
	public void setffmpegPath(String path) {
		FFmpegPath = path;
	}

	public static void audioVideoSynthesis(String videoPath, String audioPath, String targetPath) {
		try {
			String command = new File(FFmpegPath).getAbsolutePath() + "/ffmpeg -i " + videoPath + " -i " + audioPath
					+ " -c:v copy -c:a aac -strict experimental " + targetPath;
			Process Process = Runtime.getRuntime().exec(command);
			new Thread() {
				public void run() {
					try {
						BufferedReader br = new BufferedReader(new InputStreamReader(Process.getErrorStream()));
						StringBuffer sb = new StringBuffer();
						String line = "";
						while ((line = br.readLine()) != null) {
							sb.append(line);
							System.out.println(line);
						}
						br.close();
						Process.destroy();
						new File(videoPath).delete();
						new File(audioPath).delete();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void videoSnapshot(String videoPath, String imagePath) {
		try {
			String command = new File(FFmpegPath).getAbsolutePath() + "/ffmpeg -i " + videoPath
					+ " -r 1 -ss:00:30:00 -t 10 -q:v 2 -f image2 " + imagePath + "\\image-%d.jpeg";
			Runtime.getRuntime().exec(command);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static MultimediaInfo getVideoInfo(File file) {
		try {
			MultimediaObject object = new MultimediaObject(file);
			return object.getInfo();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}

	public static void main(String[] args) {
		List<File> list = new ArrayList<File>();
		// FileUtils.getPathFileList("D:\\pornhub", list);
		FileUtils.getPathFileList("D:\\javfinder", list);
		FileUtils.getPathFileList("D:\\eporner", list);
		FileUtils.getPathFileList("F:\\eporner", list);
		// FileUtils.getPathFileList("F:\\pornhub", list);
		List<String> nameList = new ArrayList<String>();
		for (File file : list) {
			MultimediaInfo MultimediaInfo = getVideoInfo(file);
			if (MultimediaInfo == null) {
				continue;
			} else {
				long duration = MultimediaInfo.getDuration();
				int height = MultimediaInfo.getVideo().getSize().getHeight();
				int width = MultimediaInfo.getVideo().getSize().getWidth();
				if (duration < 1000 * 60 * 15) {
					// file.delete();
					System.out.println(file.getAbsolutePath());
					nameList.add(file.getAbsolutePath());
				}
			}
			// System.out.println(JSON.toJSONString(MultimediaInfo));
		}
		for (String name : nameList) {
			System.out.println(name);
		}
		System.out.println("-----------------------");
		System.out.println(nameList.size());
	}
}
