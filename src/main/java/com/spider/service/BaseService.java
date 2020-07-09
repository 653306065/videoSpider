package com.spider.service;

import java.lang.reflect.ParameterizedType;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

public abstract class BaseService<T> {

    @Autowired
    protected MongoTemplate mongoTemplate;

    private Class<T> clasz;

    public BaseService() {
        clasz = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    public long count(String key, String value) {
        return mongoTemplate.count(new Query(Criteria.where(key).is(value)), clasz);
    }

    public List<T> findAll(){
        return mongoTemplate.findAll(clasz);
    }
}
