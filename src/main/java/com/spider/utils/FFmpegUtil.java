package com.spider.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class FFmpegUtil {

	public static String FFmpegPath = "C:\\ffmpeg\\bin\\";
	
	@Value("${FFmpegPath}")
	public void setffmpegPath(String path)  {
		FFmpegPath=path;
	}

	public static void audioVideoSynthesis(String videoPath, String audioPath, String targetPath) {
		try {
			String  command =new File(FFmpegPath).getAbsolutePath() + "/ffmpeg -i "+ videoPath + " -i " + audioPath + " -c:v copy -c:a aac -strict experimental " + targetPath;
			Runtime.getRuntime().exec(command);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void videoSnapshot (String videoPath,String imagePath) {
		try {
			String  command =new File(FFmpegPath).getAbsolutePath() + "/ffmpeg -i "+ videoPath+" -r 1 -ss:00:30:00 -t 10 -q:v 2 -f image2 "+imagePath+"\\image-%d.jpeg";
			Runtime.getRuntime().exec(command);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	

	public static void main(String[] args) {
		videoSnapshot ("D:\\youtube\\test.mp4","D:\\youtube");
	}
}
