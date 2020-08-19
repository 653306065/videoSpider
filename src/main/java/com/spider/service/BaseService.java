package com.spider.service;

import java.lang.reflect.ParameterizedType;
import java.util.List;

import com.spider.entity.Video;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

public abstract class BaseService<T> {

    @Autowired
    protected MongoTemplate mongoTemplate;

    private Class<T> clazz;

    public BaseService() {
        clazz = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    public long count(String key, String value) {
        return mongoTemplate.count(new Query(Criteria.where(key).is(value)), clazz);
    }

    public List<T> findAll(){
        return mongoTemplate.findAll(clazz);
    }

    public List<T> findBykeyValue(String key,String value){
        Query query = new Query(Criteria.where(key).is(value));
        return  mongoTemplate.find(query,clazz);
    }

    public T findOneByRegex(String key,String value){
        Query query = new Query(Criteria.where(key).regex(value));
        return  mongoTemplate.findOne(query,clazz);
    }

    public List<T> findListByRegex(String key,String value){
        Query query = new Query(Criteria.where(key).regex(value));
        return  mongoTemplate.find(query,clazz);
    }

    public void insert(T t){
        mongoTemplate.insert(t);
    }
}
