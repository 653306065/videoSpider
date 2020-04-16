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
	
	private Logger logger =LoggerFactory.getLogger(DownloadThread.class);

	private String httpUrl;

	private Map<String, String> header;

	private Proxy proxy;

	private long startByte;

	private long endByte;

	private File file;
	
	private volatile long pieceDownloadByte=0;
	
	private AtomicLong downloadByte;

	public DownloadThread(String httpUrl, Map<String, String> header, Proxy proxy, long startByte, long endByte,
			File file,AtomicLong downloadByte) {
		this.httpUrl = httpUrl;
		this.header = header;
		this.proxy = proxy;
		this.startByte = startByte;
		this.endByte = endByte;
		this.file = file;
		this.downloadByte=downloadByte;
	}

	public void run() {
		try {
			Map<String,String> newheader=new HashMap<String,String>();
			if(header!=null) {
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
				pieceDownloadByte=pieceDownloadByte+i;
				if (i == -1) {
					break;
				} else {
					raf.write(bytes, 0, i);
				}
			}
			raf.close();
			in.close();
			//logger.info(Thread.currentThread().getName() + ",下载完成");
		} catch (Exception e) {
			e.printStackTrace();
//			if("\\n not found: limit=0 content=…".endsWith(e.getLocalizedMessage())||"unexpected end of stream".endsWith(e.getLocalizedMessage())  ) {
//				return;
//			}
			//logger.info(Thread.currentThread().getName() + ",下载异常");
			downloadByte.addAndGet(0-pieceDownloadByte);
			new DownloadThread(this.httpUrl, this.header, this.proxy, this.startByte, this.endByte, this.file,downloadByte).run();
		}

	}
}
