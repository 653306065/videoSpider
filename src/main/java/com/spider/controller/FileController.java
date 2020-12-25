package com.spider.controller;

import com.spider.utils.FileUtils;
import com.spider.vo.ResponseVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
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
        if(!targetPathFile.exists()){
            targetPathFile.mkdirs();
        }
        File keyPathFile = new File(keyPath);
        if (!sourcePathFile.exists() || !keyPathFile.exists()) {
            return ResponseVo.failure(-1, "文件不存在");
        }
        List<File> allBook = new ArrayList<>();
        FileUtils.getPathFileList(sourcePath, allBook);
        List<String> keyList = FileUtils.readTxt(keyPath, "UTF-8");
        List< Map<String,Object>> result = allBook.stream().parallel().map(book -> {
            Map<String, Object> map = new HashMap<>();
            AtomicInteger keyCount = new AtomicInteger(0);
            List<String> textList = new ArrayList<>();
            textList.addAll(FileUtils.readTxt(book.getAbsolutePath(), "GB18030"));
            textList.addAll(FileUtils.readTxt(book.getAbsolutePath(), "GBK"));
            textList.addAll(FileUtils.readTxt(book.getAbsolutePath(), "GB2312"));
            textList.addAll(FileUtils.readTxt(book.getAbsolutePath(), "ASCII"));
            textList.addAll(FileUtils.readTxt(book.getAbsolutePath(), "UTF-8"));
            textList.addAll(FileUtils.readTxt(book.getAbsolutePath(), "BIG5"));
            List<String> matchKey = new ArrayList<>();
            List<String> matchLine=new ArrayList<>();
            textList.stream().distinct().forEach(line -> {
                keyList.stream().distinct().filter(key->!key.startsWith("#")).forEach(key -> {
                    if (line.contains(key)) {
                        keyCount.incrementAndGet();
                        matchKey.add(key);
                        matchLine.add(line);
                        logger.info("{},{},{}",book.getAbsolutePath(),key,line);
                    }
                });
            });
            if(keyCount.get()>matchCount){
                map.put("savePath", book.getAbsolutePath());
                map.put("keyCount", keyCount.get());
                map.put("matchKey", new HashSet<>(matchKey));
                map.put("matchLine", new HashSet<>(matchLine));
                FileUtils.FileCopy(book.getAbsolutePath(),targetPath+book.getName());
                return map;
            }
            return null;
        }).filter(Objects::nonNull).collect(Collectors.toList());
        return ResponseVo.succee(result);
    }

}
