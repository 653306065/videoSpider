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
import java.io.Reader;
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

	public static List<File> fileList = new ArrayList<File>();

	public static List<File> getPathFileList(String path,List<File> list) {
		File files = new File(path);
		if (!files.exists() || !files.isDirectory() || files.listFiles().length == 0) {
			return null;
		} else {
			for (File file : files.listFiles()) {
				if (file.isDirectory()) {
					getPathFileList(file.getAbsolutePath(),list);
				} else {
					list.add(file);
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
			// System.out.println(file.getName());
			if (file.getName().indexOf(key) != -1) {
				System.out.println(file.getName());
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

	static String[] textKeyArr = { "肏妈妈", "肏得妈妈", "妈妈的性欲", "妈妈好爽", "娘亲被你弄得好舒服", "妈妈被你弄得好舒服", "母亲浪叫", "母亲的肉穴", "妈妈的阴道",
			"妈妈爽死", "大鸡巴儿子", "母亲骚穴", "母亲的屁股", "美母情动", "妈妈，好舒服", "亲生儿子的肉棒", "玩妈妈的乳房", "妈妈的小屄", "妈妈的乳头", "母亲的小穴", "骚妈妈",
			"妈妈舒服死了", "母亲的阴唇", "妈妈的子宫", "射入母亲深处", "儿子大鸡巴", "儿子的肉棒好舒服", "奸淫妈妈", "妈妈含着儿子的肉棒", "妈妈，你舒服吗", "你又欺负妈妈",
			"妈妈要飞了", "儿子的肉棒", "母亲阴道", "妈妈美穴", "母亲美穴", "爽死妈了", "母亲花蕊", "母亲小浪穴", "母亲浪穴", "妈妈小浪穴", "妈妈浪穴", "娘亲浪穴", "娘亲小浪穴",
			"妈妈双乳", "妈妈乳峰", "妈妈的内裤", "妈妈的乳房", "妈妈的禁地", "妈妈的屁股", "小骚穴妈妈", "吃着儿子的肉棒", "大肉棒享受妈妈", "舔死你妈妈", "亲生儿子的淫辱",
			"妈妈，来！把屁股翘高一点", "妈妈要死了", "快饶了妈妈吧", "妈妈不行了", "被儿子插入", "征服妈妈", "插入妈妈", "老妈的玉乳", "母亲的小骚穴", "妈妈的骚屄", "妈要丢了",
			"妈被你插得好舒服", "饶了妈", "妈，要丢了", "亲母的蜜穴", "母子乱伦", "被自己儿子插入了", "老妈蜜穴", "亲生儿子奸淫", "亲娘的阴道", "奸淫老妈", "强奸妈妈", "强奸母亲",
			"儿子阳精", "儿子性交", "来操娘", "操妈妈", "妈妈蜜穴", "母親子宮", "妈妈痒", "母子激情", "妈妈高潮", "妈妈的肉屄", "分开了老妈的双腿", "亲儿子你真棒",
			"好儿子…好舒服", "占有妈妈", "母亲的穴" };

	// static String[] textKeyArr= {"娘亲被你弄得好舒服"};

	// static String[] textKeyArr = { "大肉棒", "阴茎", "肏屄", "阴唇", "小骚穴", "大鸡巴",
	// "粗长的肉棒", "肉穴", "大龟头", "阴户", "龟头", "玉茎", "花蕊",
	// "美穴", "小浪穴", "阴蒂", "阴道", "淫水", "蜜穴", "子宮", "骚屄", "浪穴", "肉棒", "操屄", "阳精",
	// "射精", "交合处", "龙枪", "生命精华" };

	public static List<File> getIncestText(List<File> fileList) throws Exception {
		List<File> incestList = new ArrayList<File>();
		for (File file : fileList) {
			InputStream in = new FileInputStream(file);
			// Reader reader=new InputStreamReader(in,"GB2312");
			Reader reader = new InputStreamReader(in, "UTF-8");
			// Reader reader = new InputStreamReader(in,
			// codeString(file.getAbsolutePath()));
			BufferedReader buffReader = new BufferedReader(reader);
			// System.err.println(file.getName()+":");
			tag: while (true) {
				String line = buffReader.readLine();
				if (line == null) {
					break;
				} else {
					for (String key : textKeyArr) {
						if (line.indexOf(key) != -1) {
							System.out.println(file.getName() + ":" + line);
							incestList.add(file);
							in.close();
							reader.close();
							buffReader.close();
							break tag;
						}
					}
				}
			}

		}
		return incestList;
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
				// 写入一个换行
				out.write("\r\n".getBytes());
			}
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static List<String> readTxt(String path) {
		List<String> list = new ArrayList<>();
		try {
			FileInputStream fileInputStream = new FileInputStream(path);
			InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
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
				if (fileInputStream != null){
					fileInputStream.close();
					}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
