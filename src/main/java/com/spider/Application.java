package com.spider;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import com.spider.utils.FileUtils;
import com.spider.utils.ImageUtils;
import com.spider.web.Eporner;
import com.spider.web.Hanime;
import com.spider.web.Longteng;
import com.spider.web.Wallhaven;

@SpringBootApplication
public class Application {

	static Logger logger = LoggerFactory.getLogger(Application.class);

	public static void main(String[] args) {
		ConfigurableApplicationContext context = SpringApplication.run(Application.class);
		Eporner eporner=context.getBean(Eporner.class);
//		Wallhaven wallhaven=context.getBean(Wallhaven.class);
//		Hanime hanime=context.getBean(Hanime.class);
//		hanime.Download_nsfw_general_Image();
//		hanime.Download_irl_3d_Image();
//		wallhaven.downloadNSFWPeople4K();
//		wallhaven.downloadAnime2k();
//		wallhaven.downloadNSFWPeople1080p();
		eporner.downloadByCategory("group-sex");
//		List<File> list=new ArrayList<File>();
//		FileUtils.getPathFileListBySuffix("F:\\hanime\\nsfw_general", list, "jpg");
//		FileUtils.getPathFileListBySuffix("F:\\hanime\\nsfw_general", list, "jpeg");
//		FileUtils.getPathFileListBySuffix("F:\\hanime\\nsfw_general", list, "png");
//		for(File file:list) {
//			int height = ImageUtils.getImgHeight(file);
//			int width = ImageUtils.getImgWidth(file);
//			if(height*width<1280*720) {
//				file.delete();
//				System.out.println(file.getName());
//			}
//		}

	}
}
