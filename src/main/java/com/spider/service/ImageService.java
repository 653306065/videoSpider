package com.spider.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.spider.entity.Image;

@Service
public class ImageService {

	@Autowired
	private MongoTemplate mongoTemplate;

	public Image findByName(String name) {
		Query query = new Query(Criteria.where("name").is(name));
		return mongoTemplate.findOne(query, Image.class);
	}

	public Image insert(Image video) {
		return mongoTemplate.insert(video);
	}
	
	public List<Image> findAll(){
		return mongoTemplate.findAll(Image.class);
	}

	public Image findByMd5(String md5) {
		Query query = new Query(Criteria.where("md5").is(md5));
		return mongoTemplate.findOne(query, Image.class);
	}
	
	public Image findByUrl(String url) {
		Query query = new Query(Criteria.where("source").is(url));
		return mongoTemplate.findOne(query, Image.class);
	}
}
