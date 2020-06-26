package com.spider.service;

import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import com.spider.entity.By114BT;

@Service
public class By114BTService extends BaseService<By114BT>{

	public By114BT findByUrl(String url) {
		Query query = new Query(Criteria.where("url").is(url));
		return mongoTemplate.findOne(query, By114BT.class);
	}
	
	public void save(By114BT bt) {
		mongoTemplate.save(bt);
	}

}
