package com.spider.utils;

import java.io.BufferedInputStream;
import org.apache.commons.codec.binary.Hex;
import java.security.MessageDigest;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FileUtils {

	public static void changeFileMd5(String oldFile, String newFile) throws Exception {
		String uid = UUID.randomUUID().toString().replaceAll("-", "");
		System.err.println(oldFile + ",开始修改，uid:" + uid);
		File file = new File(newFile);
		if (!file.exists()) {
			if (!new File(file.getParent()).exists()) {
				new File(file.getParent()).mkdirs();
			}
			file.createNewFile();
		} else {
			return;
		}
		InputStream in = new FileInputStream(oldFile);
		OutputStream out = new FileOutputStream(newFile);
		byte[] bytes = new byte[2048];
		while (true) {
			int i = in.read(bytes);
			if (i == -1) {
				if (file.length() > 1024 * 1 * 1000) {
					out.write(uid.getBytes());
				}
				in.close();
				out.flush();
				out.close();
				System.err.println(oldFile + ",修改完成");
				break;
			} else {
				out.write(bytes, 0, i);
			}
		}
	}

	

	public static List<File> getPathFileList(String path, List<File> list) {
		File files = new File(path);
		if (!files.exists() || !files.isDirectory() || files.listFiles().length == 0) {
			return null;
		} else {
			for (File file : files.listFiles()) {
				if (file.isDirectory()) {
					getPathFileList(file.getAbsolutePath(), list);
				} else {
					list.add(file);
				}
			}
		}
		return list;
	}
	
	public static List<File> getPathFileListBySuffix(String path, List<File> list,String suffix) {
		File files = new File(path);
		if (!files.exists() || !files.isDirectory() || files.listFiles().length == 0) {
			return null;
		} else {
			for (File file : files.listFiles()) {
				if (file.isDirectory()) {
					getPathFileListBySuffix(file.getAbsolutePath(), list,suffix);
				} else {
					if(file.getName().endsWith(suffix)) {
						list.add(file);
					}
				}
			}
		}
		return list;
	}
	

	public static void changeFileMd5(File oldFile, File newFile) throws Exception {
		InputStream in = new FileInputStream(oldFile);
		OutputStream out = new FileOutputStream(newFile);
		byte[] bytes = new byte[1024];
		while (true) {
			if (in.read(bytes) == -1) {
				String uid = UUID.randomUUID().toString().replaceAll("-", "");
				System.err.println(uid);
				out.write(uid.getBytes());
				in.close();
				out.flush();
				out.close();
				System.err.println(oldFile.getName() + ",修改完成");
				System.err.println("加密后名称为：" + newFile);
				break;
			} else {
				out.write(bytes);
			}
		}
	}

	public static void pathFileCopy(String oldPath, String newPath) throws Exception {
		File oldPathFile = new File(oldPath);
		if (!oldPathFile.exists() || !oldPathFile.isDirectory()) {
			return;
		}
		File newPathFile = new File(newPath);
		if (!newPathFile.exists()) {
			newPathFile.mkdirs();
		}
		// DesUtils des = new DesUtils("6");
		File[] fileArr = oldPathFile.listFiles();
		for (File file : fileArr) {
			String oldFile = file.getAbsolutePath();
			String base64 = getFileNameWithoutSuffix(file.getName()) + ".${" + getFileFormatName(oldFile) + "}$";
			String newFile = newPath + File.separator + base64;
			// System.out.println(base64);
			changeFileMd5(oldFile, newFile);
		}

	}

	public static void deleteFileByKey(String path, String key) {
		File files = new File(path);
		if (!files.exists() || !files.isDirectory() || files.listFiles().length == 0) {
			return;
		}
		File[] fileArr = files.listFiles();
		for (File file : fileArr) {
			if (file.getName().indexOf(key) != -1) {
				deleteFile(file.getAbsolutePath());
			}
		}
	}

	public static void deleteFile(String path) {
		File file = new File(path);
		if (file.isDirectory()) {
			File[] fileArr = file.listFiles();
			for (File Tfile : fileArr) {
				deleteFile(Tfile.getAbsolutePath());
			}
		}
		file.delete();
	}

	public static void FileCopy(String oldPath, String newPath) throws IOException {
		InputStream in = new FileInputStream(oldPath);
		OutputStream out = new FileOutputStream(newPath);
		byte[] bytes = new byte[1024];
		while (true) {
			if (in.read(bytes) == -1) {
				in.close();
				out.flush();
				out.close();
				break;
			} else {
				out.write(bytes);
			}
		}
	}

	public static String codeString(String fileName) throws Exception {
		BufferedInputStream bin = new BufferedInputStream(new FileInputStream(fileName));
		int p = (bin.read() << 8) + bin.read();
		String code = null;
		switch (p) {
		case 0xefbb:
			code = "UTF-8";
			break;
		case 0xfffe:
			code = "Unicode";
			break;
		case 0xfeff:
			code = "UTF-16BE";
			break;
		default:
			code = "GBK";
		}
		bin.close();
		return code;
	}

	public static String getFileNameWithoutSuffix(String fileName) {
		return fileName.substring(0, fileName.lastIndexOf("."));
	}

	public static String getFileFormatName(String fileName) {
		return fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length());
	}

	public static void saveTxt(String path, List<String> lineList) {
		try {
			OutputStream out = new FileOutputStream(path, true);
			for (String str : lineList) {
				out.write(str.getBytes());
				out.write("\r\n".getBytes());
			}
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static List<String> readTxt(String path,String charset) {
		List<String> list = new ArrayList<>();
		try {
			FileInputStream fileInputStream = new FileInputStream(path);
			InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream,charset);
			BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
			while (true) {
				String line = bufferedReader.readLine();
				if (line == null) {
					break;
				}
				list.add(line);
			}
			bufferedReader.close();
			inputStreamReader.close();
			fileInputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;

	}

	public static String getMD5(File file) {
		FileInputStream fileInputStream = null;
		try {
			MessageDigest MD5 = MessageDigest.getInstance("MD5");
			fileInputStream = new FileInputStream(file);
			byte[] buffer = new byte[8192];
			int length;
			while ((length = fileInputStream.read(buffer)) != -1) {
				MD5.update(buffer, 0, length);
			}
			return new String(Hex.encodeHex(MD5.digest()));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			try {
				if (fileInputStream != null) {
					fileInputStream.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void main(String[] args) {
		List<File> list=getPathFileList("D:\\BaiduNetdiskDownload\\book", new ArrayList<File>());
		List<String> strList=readTxt("D:\\BaiduNetdiskDownload\\key.txt","GBK");
		List<String> lineList=new ArrayList<String>();
		for(File text:list) {
			try {
				System.out.println(text.getName());
				FileInputStream fileInputStream = new FileInputStream(text);
				InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream,"GBK");//GBK UTF-8 GB2312 GB13000 GB18030 BIG5
				BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
				int hit=0;
				List<String> bookLine=new ArrayList<String>();
				bookLine.add(text.getName());
				while (true) {
					String line = bufferedReader.readLine();
					if(line!=null) {
						line=line.replace(" ", "");
					}else {
						bookLine.clear();
						break;
					}
					List<String> bookList=new ArrayList<String>();
					bookList.add(text.getName());
					for(String key:strList) {
						if(line.indexOf(key)!=-1) {
							bookLine.add(key);
							bookLine.add(line);
							bookLine.add("                              ");
							hit++;
							System.out.println(hit);
							if(line.length()>1000) {
								String str=line.substring(0, 1000);
								System.out.println(str);
							}else {
								System.out.println(line);
							}
							//break;
						}
					}
					if(hit>=3) {
						bookLine.add("------------------------------");
						bookLine.add("                              ");
						bufferedReader.close();
						text.renameTo(new File("D:\\BaiduNetdiskDownload\\true\\"+text.getName()));
						lineList.addAll(bookLine);
						break;
				    }
				}
				System.out.println("------------------------------");
				bufferedReader.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		saveTxt("D:\\BaiduNetdiskDownload\\keyLine.txt", lineList);
	}
}
