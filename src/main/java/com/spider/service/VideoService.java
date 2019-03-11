package com.spider.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import com.spider.entity.Video;

@Service
public class VideoService {

	@Autowired
	private MongoTemplate mongoTemplate;

	public Video findByName(String name) {
		Query query = new Query(Criteria.where("name").is(name));
		return mongoTemplate.findOne(query, Video.class);
	}

	public Video insert(Video video) {
		return mongoTemplate.insert(video);
	}
	
	public Video findByMd5(String md5) {
		Query query = new Query(Criteria.where("md5").is(md5));
		return mongoTemplate.findOne(query, Video.class);
	}
}
