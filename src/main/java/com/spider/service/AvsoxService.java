package com.spider.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.spider.entity.AvInfo;

@Service
public class AvsoxService {

    @Autowired
    private MongoTemplate mongoTemplate;

    public void insert(AvInfo info) {
        mongoTemplate.insert(info);
    }

    public AvInfo findByCode(String code) {
        Query query = new Query(Criteria.where("code").is(code));
        return mongoTemplate.findOne(query, AvInfo.class);
    }

}
