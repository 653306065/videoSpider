package com.spider.service;

import com.alibaba.fastjson.JSON;
import com.spider.entity.FaceInfo;
import com.spider.utils.FFmpegUtil;
import com.spider.utils.FaceUtil;
import com.spider.utils.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import com.spider.entity.Video;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

@Service
public class VideoService extends BaseService<Video> {

    @Autowired
    private MongoTemplate mongoTemplate;

    public Video findByName(String name) {
        Query query = new Query(Criteria.where("name").is(name));
        return mongoTemplate.findOne(query, Video.class);
    }

    public Video findByMd5(String md5) {
        Query query = new Query(Criteria.where("md5").is(md5));
        return mongoTemplate.findOne(query, Video.class);
    }

    public Video findByVideoUrl(String videoUrl) {
        Query query = new Query(Criteria.where("videoUrl").is(videoUrl));
        return mongoTemplate.findOne(query, Video.class);
    }

    public Video findBySourceUrl(String sourceUrl) {
        Query query = new Query(Criteria.where("sourceUrl").is(sourceUrl));
        return mongoTemplate.findOne(query, Video.class);
    }

    public Double videoScore(String videoId, Integer imageCount, double threshold, boolean isDelete) {
        Video findVideo = this.findById(videoId);
        if (Objects.nonNull(findVideo)&&Objects.nonNull(findVideo.getMultimediaInfo())) {
            long duration = findVideo.getMultimediaInfo().getDuration();
            long size = duration / imageCount;
            File file = new File(findVideo.getSavePath());
            String path = file.getParentFile().getAbsolutePath() + File.separator + "temp";
            logger.info("{},截图路径:{}",findVideo.getSavePath(),path);
            new File(path).mkdirs();
            for (int i = 0; i < imageCount; i++) {
                FFmpegUtil.videoSnapshot(findVideo.getSavePath(), path, findVideo.getName() + "_" + i, (duration - (size * (i + 1))) / 1000, 1);
            }
            List<File> fileList = new ArrayList<>();
            FileUtils.getPathFileList(path, fileList);
            List<FaceInfo> faceInfos = fileList.stream().sequential().flatMap(image -> {
                try {
                    List<FaceInfo> faceInfoList = FaceUtil.faceInfo(org.apache.commons.io.FileUtils.readFileToByteArray(image));
                    if (!CollectionUtils.isEmpty(faceInfoList)) {
                        System.out.println(JSON.toJSONString(faceInfoList.stream().filter(faceInfo -> faceInfo.getGender().equals("Female")).collect(Collectors.toList())));
                        return faceInfoList.stream();
                    }
                    Thread.sleep(1100);
                    return null;
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }).filter(Objects::nonNull).collect(Collectors.toList());
            System.out.println(JSON.toJSONString(faceInfos));
            if (faceInfos.stream().filter(faceInfo -> faceInfo.getGender().equals("Female")).count() == 0) {
                logger.info("未匹配到面部，删除视频:{}", findVideo.getSavePath());
                file.delete();
                fileList.stream().forEach(image -> image.delete());
                new File(path).delete();
                return null;
            }
            findVideo.setFaceInfoList(faceInfos.stream().filter(faceInfo -> faceInfo.getGender().equals("Female")).collect(Collectors.toList()));
            double avg = faceInfos.stream().filter(faceInfo -> faceInfo.getGender().equals("Female")).mapToDouble(faceInfo -> faceInfo.getFemaleScore() + faceInfo.getMaleScore()).average().getAsDouble() / 2;
            double max = faceInfos.stream().filter(faceInfo -> faceInfo.getGender().equals("Female")).flatMapToDouble(faceInfo -> DoubleStream.of(faceInfo.getFemaleScore(), faceInfo.getMaleScore())).max().getAsDouble();
            double min = faceInfos.stream().filter(faceInfo -> faceInfo.getGender().equals("Female")).flatMapToDouble(faceInfo -> DoubleStream.of(faceInfo.getFemaleScore(), faceInfo.getMaleScore())).min().getAsDouble();
            long faceNum = faceInfos.stream().filter(faceInfo -> faceInfo.getGender().equals("Female")).flatMapToDouble(faceInfo -> DoubleStream.of(faceInfo.getFemaleScore(), faceInfo.getMaleScore())).count();
            double median = faceInfos.stream().filter(faceInfo -> faceInfo.getGender().equals("Female")).flatMapToDouble(faceInfo -> DoubleStream.of(faceInfo.getFemaleScore(), faceInfo.getMaleScore())).sorted().skip(faceNum / 2).limit(1).max().getAsDouble();
            logger.info("{},平均评分:{},最高评分:{},最低评分:{},中位数:{}", findVideo.getSavePath(), avg, max, min,median);
            findVideo.setAvgFaceScore(avg);
            findVideo.setMaxFaceScore(max);
            findVideo.setMinFaceScore(min);
            findVideo.setMedianFaceScore(median);
            this.updateById(findVideo);
            if (median < threshold && isDelete) {
                logger.info("颜值过低，删除视频," + findVideo.getSavePath());
                file.delete();
            }
            FileUtils.deleteDir(path);
            return avg;
        }
        return null;
    }
}
