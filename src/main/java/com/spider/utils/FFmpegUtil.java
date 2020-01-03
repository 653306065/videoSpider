package com.spider.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

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
			Runtime.getRuntime().exec(command);
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
//		List<File> list=new ArrayList<E>();
//		FileUtils.getPathFileList("C:\\eporner", list)
//		
//		
//		File file=new File("D:\\javfinder\\video\\STAGE2MEDIA S2MBD-008 She got a gorgeous look hot sexy appeal and above all.mp4");
//		MultimediaInfo MultimediaInfo=getVideoInfo(file);
//		MultimediaInfo.getDuration();
//		System.out.println(JSON.toJSONString(MultimediaInfo));
	}
}
