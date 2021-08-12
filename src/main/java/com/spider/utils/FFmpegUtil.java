package com.spider.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ws.schild.jave.MultimediaInfo;
import ws.schild.jave.MultimediaObject;


@Component
public class FFmpegUtil {

    private static Logger logger = LoggerFactory.getLogger(FFmpegUtil.class);

    public static String FFmpegPath = "C:\\ffmpeg\\bin\\";

    @Value("${FFmpegPath}")
    public void setffmpegPath(String path) {
        FFmpegPath = path;
    }

    public static void audioVideoSynthesis(String videoPath, String audioPath, String targetPath) {
        try {
            String command = new File(FFmpegPath).getAbsolutePath() + "/ffmpeg -i \"" + videoPath + "\" -i \"" + audioPath
                    + "\" -c:v copy -c:a aac -strict experimental \"" + targetPath + "\"";
            Process Process = Runtime.getRuntime().exec(command);
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
                logger.info("{},{},合并完成,合成路径:{}", videoPath, audioPath, targetPath);
            } catch (Exception e) {
                e.printStackTrace();
            }
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

    public static void videoSnapshot(String videoPath, String imagePath, String fileName, long time, int count) {
        try {
            String command = new File(FFmpegPath).getAbsolutePath() + "/ffmpeg -ss " + time + " -i  \"" + videoPath + "\"" +
                    " -vframes " + count + " \"" + imagePath + "\\" + fileName + "_image-%d.jpg\"";
            Process Process = Runtime.getRuntime().exec(command);
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
                logger.info("截图完成");
            } catch (Exception e) {
                e.printStackTrace();
            }
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

    public static void downloadM3U8(String url, String savePath) {
        try {
            if (!new File(savePath).getParentFile().exists()) {
                new File(savePath).getParentFile().mkdirs();
            }
            String command = new File(FFmpegPath).getAbsolutePath() + "/ffmpeg -i \"" + url + "\" -c copy \"" + savePath + "\" ";
            Process Process = Runtime.getRuntime().exec(command);
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(Process.getErrorStream()));
                String line = "";
                while ((line = br.readLine()) != null) {
                    System.out.println(line);
                }
                br.close();
                Process.destroy();
                logger.info("下载完成");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void mergeTsFile(String tsPath, String videoPath) {
        try {
            List<File> list = new ArrayList<>();
            FileUtils.getPathFileList(tsPath, list);
            List<String> textList = list.stream().sorted(Comparator.comparing(file -> Integer.valueOf(file.getName().split("\\.")[0]))).map(file -> "file '" + file.getAbsolutePath() + "'").collect(Collectors.toList());
            File file = File.createTempFile("tsLiST", ".txt");
            FileUtils.saveTxt(file.getAbsolutePath(), textList);
            File videoFile=new File(videoPath);
            File tempFile=new File(videoFile.getParentFile().getAbsolutePath()+File.separator+ UUID.randomUUID() +".mp4");
            String command = new File(FFmpegPath).getAbsolutePath() + "/ffmpeg -f concat -safe 0 -i \"" + file.getAbsolutePath() + "\" -c copy \"" + tempFile.getAbsolutePath() + "\"";
            Process Process = Runtime.getRuntime().exec(command);
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(Process.getErrorStream()));
                String line = "";
                while ((line = br.readLine()) != null) {
                    System.out.println(line);
                }
                br.close();
                Process.destroy();
                tempFile.renameTo(videoFile);
            } catch (Exception e) {
                e.printStackTrace();
            }
            file.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        mergeTsFile("C:\\wandusp\\uncensored\\2021-07-25\\temp\\1204b271c08a424ea091013203ab1a67", "C:\\test\\一本道1pon-021821-001 グラマラス                 森田みゆ.mp4");
    }
}
