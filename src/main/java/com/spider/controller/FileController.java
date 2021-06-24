package com.spider.controller;

import com.spider.utils.ExchangeUtil;
import com.spider.utils.FileUtils;
import com.spider.vo.ResponseVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Api(tags = "文件接口")
@RequestMapping("/api/file")
@RestController
public class FileController extends BaseController {

    @ApiOperation("text根据key移动到指定目录")
    @GetMapping("/text/findAndMoveByKey")
    public ResponseVo<Object> findAndMoveByKey(@RequestParam("sourcePath") String sourcePath,
                                               @RequestParam("targetPath") String targetPath,
                                               @RequestParam("keyPath") String keyPath,
                                               @RequestParam("matchCount") Integer matchCount) {
        File sourcePathFile = new File(sourcePath);
        File targetPathFile = new File(targetPath);
        if (!targetPathFile.exists()) {
            targetPathFile.mkdirs();
        }
        File keyPathFile = new File(keyPath);
        if (!sourcePathFile.exists() || !keyPathFile.exists()) {
            return ResponseVo.failure(-1, "文件不存在");
        }
        List<File> allBook = new ArrayList<>();
        FileUtils.getPathFileList(sourcePath, allBook);
        List<String> keyList = FileUtils.readTxt(keyPath, "UTF-8");
        List<Map<String, Object>> result = allBook.stream().map(book -> {
            System.out.println("-----------------------------------------");
            Map<String, Object> map = new HashMap<>();
            AtomicInteger keyCount = new AtomicInteger(0);
            String charset = getTextCharset(book);
            if (!StringUtils.isBlank(charset)) {
                List<String> textList = FileUtils.readTxt(book.getAbsolutePath(), charset);
                List<String> matchKey = new ArrayList<>();
                List<String> matchLine = new ArrayList<>();
                textList.stream().distinct().forEach(line -> {
                    keyList.stream().distinct().forEach(key -> {
                        if (Pattern.matches(key, line)) {
                            keyCount.incrementAndGet();
                            matchKey.add(key);
                            matchLine.add(line);
                            if (line.length() > 200) {
                                System.out.println(book.getName() + "," + line.substring(0, 200));
                            } else {
                                System.out.println(book.getName() + "," + line);
                            }
                        }
                    });
                });
                if (keyCount.get() > matchCount) {
                    map.put("savePath", book.getAbsolutePath());
                    map.put("keyCount", keyCount.get());
                    map.put("matchKey", new HashSet<>(matchKey));
                    map.put("matchLine", new HashSet<>(matchLine));
                    if (new File(targetPath + book.getName()).exists()) {
                        book.delete();
                    } else {
                        book.renameTo(new File(targetPath + book.getName()));
                    }
                    return map;
                }
                return null;
            }
            return null;
        }).filter(Objects::nonNull).collect(Collectors.toList());
        return ResponseVo.succee(result);
    }

    public String getTextCharset(File file) {
        try {
            ExchangeUtil exchangeUtil = new ExchangeUtil();
            return ExchangeUtil.javaname[exchangeUtil.detectEncoding(file)];
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
