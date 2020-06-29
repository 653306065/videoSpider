package com.spider.utils;

import java.io.BufferedInputStream;

import org.apache.commons.codec.binary.Hex;
import org.springframework.util.DigestUtils;

import com.alibaba.fastjson.JSON;

import java.security.MessageDigest;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

    public static List<File> getPathFileListBySuffix(String path, List<File> list, String suffix) {
        File files = new File(path);
        if (!files.exists() || !files.isDirectory() || files.listFiles().length == 0) {
            return null;
        } else {
            for (File file : files.listFiles()) {
                if (file.isDirectory()) {
                    getPathFileListBySuffix(file.getAbsolutePath(), list, suffix);
                } else {
                    if (file.getName().endsWith(suffix)) {
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

    public static List<String> readTxt(String path, String charset) {
        List<String> list = new ArrayList<>();
        try {
            FileInputStream fileInputStream = new FileInputStream(path);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, charset);
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

    public static String getFileMD5String(File file) {
        try {
            MessageDigest MD5 = MessageDigest.getInstance("MD5");
            FileInputStream in = new FileInputStream(file);
            FileChannel ch = in.getChannel();
            MappedByteBuffer byteBuffer = ch.map(FileChannel.MapMode.READ_ONLY, 0, file.length());
            MD5.update(byteBuffer);
            in.close();
            return String.valueOf(Hex.encodeHex(MD5.digest()));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void byteToFile(byte[] bytes, String path) {
        try {
            // 根据绝对路径初始化文件
            File localFile = new File(path);
            if (!localFile.exists()) {
                localFile.getParentFile().mkdirs();
                localFile.createNewFile();
            }
            // 输出流
            OutputStream os = new FileOutputStream(localFile);
            os.write(bytes);
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getMD5(File file) {
        FileInputStream fileInputStream = null;
        try {
            MessageDigest MD5 = MessageDigest.getInstance("MD5");
            fileInputStream = new FileInputStream(file);
            byte[] buffer = new byte[1024 * 1024 * 1024 / 2];
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

    public static void deleteDir(String dirPath) {
        File file = new File(dirPath);
        if (file.isFile()) {
            file.delete();
        } else {
            File[] files = file.listFiles();
            if (files == null) {
                file.delete();
            } else {
                for (int i = 0; i < files.length; i++) {
                    deleteDir(files[i].getAbsolutePath());
                }
                file.delete();
            }
        }
    }

    /**
     * 删除window路径中不允许的字符
     *
     * @return
     */
    public static String repairPath(String path) {
        return path.replaceAll("\\|", "").replaceAll("//", "").replaceAll("/", "").replaceAll(":", "")
                .replaceAll("\\*", "").replaceAll("\\?", "").replaceAll("\"", "").replaceAll("<", "")
                .replaceAll(">", "").replaceAll("|", "");
    }

    public static void main(String[] args) {
        List<String> keys = readTxt("E:\\key1.txt", "UTF-8");
        List<File> list = new ArrayList<File>();
        getPathFileList("E:\\母子", list);
        getPathFileList("E:\\黄书", list);
        for (File file : list) {
            int time = 0;
            List<String> text = readTxt(file.getAbsolutePath(), "UTF-8");
            text.addAll(readTxt(file.getAbsolutePath(), "GBK"));
            text.addAll(readTxt(file.getAbsolutePath(), "GB2312"));
            text.addAll(readTxt(file.getAbsolutePath(), "BIG5"));
            A:
            for (String str : text) {
                for (String key : keys) {
                    if (str.indexOf(key) != -1) {
                        time++;
                        System.out.println(str);
                    }
                    if (time > 20) {
                        System.out.println(file.getAbsolutePath());
                        file.renameTo(new File("E:\\test\\" + file.getName()));
                        break A;
                    }
                }
            }
        }
        // System.out.println(JSON.toJSONString(keys));
    }
}
