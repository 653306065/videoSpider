package com.spider.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.spider.entity.By114BT;

@Service
public class By114BTService {

	@Autowired
	private MongoTemplate mongoTemplate;

	public By114BT findByUrl(String url) {
		Query query = new Query(Criteria.where("url").is(url));
		return mongoTemplate.findOne(query, By114BT.class);
	}
	
	public void save(By114BT bt) {
		mongoTemplate.save(bt);
	}

}
