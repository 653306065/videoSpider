package com.spider.utils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

public class ImageUtils {

    public static int getImgWidth(File file) {
        InputStream is = null;
        BufferedImage src = null;
        int ret = -1;
        try {
            is = new FileInputStream(file);
            src = ImageIO.read(is);
            ret = src.getWidth(null); // 得到源图宽
            is.close();
        } catch (Exception e) {
            //e.printStackTrace();
        }
        return ret;
    }

    public static int getImgHeight(File file) {
        InputStream is = null;
        BufferedImage src = null;
        int ret = -1;
        try {
            is = new FileInputStream(file);
            src = ImageIO.read(is);
            ret = src.getHeight(null); // 得到源图高
            is.close();
        } catch (Exception e) {
            //e.printStackTrace();
        }
        return ret;
    }

    public static void main(String[] args) {
        List<File> list = new ArrayList<File>();
        FileUtils.getPathFileList("F:\\hanime\\", list);
        for (File file : list) {
            if (file.getName().endsWith("gif") || file.getName().endsWith("mp4") || file.getName().endsWith("webm")) {
                continue;
            }
            try {
                int Width = getImgWidth(file);
                int Height = getImgHeight(file);
                if (Width != -1 && Height != -1 && Width * Height < 1280 * 720) {
                    file.delete();
                    System.out.println(file.getAbsolutePath());
                    file = null;

                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        System.out.println("完成");
    }
}
