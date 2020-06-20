package com.spider.utils.download;

import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.spider.utils.OKHttpUtils;

public class DownloadThread extends Thread {

	private Logger logger = LoggerFactory.getLogger(DownloadThread.class);

	private String httpUrl;

	private Map<String, String> header;

	private Proxy proxy;

	private long startByte;

	private long endByte;

	private File file;

	private long pieceDownloadByte = 0;

	private AtomicLong downloadByte;
	
	private volatile Map<String,Boolean> isStop;

	public DownloadThread(String httpUrl, Map<String, String> header, Proxy proxy, long startByte, long endByte,
			File file, AtomicLong downloadByte,Map<String,Boolean> isStop) {
		this.httpUrl = httpUrl;
		this.header = header;
		this.proxy = proxy;
		this.startByte = startByte;
		this.endByte = endByte;
		this.file = file;
		this.downloadByte = downloadByte;
		this.isStop=isStop;
	}

	public void run() {
		try {
			Map<String, String> newheader = new HashMap<String, String>();
			if (header != null) {
				newheader.putAll(header);
			}
			newheader.put("range", "bytes=" + startByte + "-" + endByte);
			InputStream in = OKHttpUtils.getInputStream(httpUrl, newheader, proxy);
			byte[] bytes = new byte[1024 * 1024 * 2];
			RandomAccessFile raf = new RandomAccessFile(file, "rw");
			raf.seek(startByte);
			while (true) {
				int i = in.read(bytes);
				downloadByte.addAndGet(i);
				pieceDownloadByte = pieceDownloadByte + i;
				if(isStop.get("isStop")) {
					logger.info("网络连接出错，线程停止运行");
					break;
				}
				if (i == -1) {
					break;
				} else {
					raf.write(bytes, 0, i);
				}
			}
			raf.close();
			in.close();
			// logger.info(Thread.currentThread().getName() + ",下载完成");
		} catch (Exception e) {
			e.printStackTrace();
			if (e.getLocalizedMessage().endsWith("\\n not found: limit=0 content=…")
					|| e.getLocalizedMessage().endsWith("unexpected end of stream")
					|| e.getLocalizedMessage().endsWith("stream was reset: INTERNAL_ERROR")
					|| e.getLocalizedMessage().endsWith("stream was reset: PROTOCOL_ERROR")) {
				isStop.put("isStop", true);
				return;
			}
			// logger.info(Thread.currentThread().getName() + ",下载异常");
			downloadByte.addAndGet(0 - pieceDownloadByte);
			new DownloadThread(this.httpUrl, this.header, this.proxy, this.startByte, this.endByte, this.file,
					downloadByte,isStop).run();
		}

	}
}
