package com.spider.service;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.spider.entity.UrlRecord;

@Service
public class UrlRecordService {

	@Autowired
	private MongoTemplate mongoTemplate;
	
	public boolean existUrl(String url) {
		Query query=new Query(Criteria.where("url").is(url));
		UrlRecord  record=mongoTemplate.findOne(query, UrlRecord.class);
		if(record==null) {
			return false;
		}
		return true;
	}
	
	public UrlRecord insert(String url) {
		UrlRecord  record =new UrlRecord();
		record.setDate(new Date());
		record.setUrl(url);
		return mongoTemplate.insert(record);
	}
}
