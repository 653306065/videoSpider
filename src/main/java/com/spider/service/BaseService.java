package com.spider.service;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.Objects;

import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

public abstract class BaseService<T> {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    protected MongoTemplate mongoTemplate;

    protected Class<T> clazz;

    public BaseService() {
        clazz = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    public long count(String key, String value) {
        return mongoTemplate.count(new Query(Criteria.where(key).is(value)), clazz);
    }

    public List<T> findAll() {
        return mongoTemplate.findAll(clazz);
    }

    public List<T> findByexists(String key, Boolean exists) {
        Query query = new Query(Criteria.where(key).exists(exists));
        return mongoTemplate.find(query, clazz);
    }

    public List<T> find(Query query) {
        return mongoTemplate.find(query, clazz);
    }

    public List<T> findBykeyValue(String key, String value) {
        Query query = new Query(Criteria.where(key).is(value));
        return mongoTemplate.find(query, clazz);
    }

    public T findOnekeyValue(String key, String value) {
        Query query = new Query(Criteria.where(key).is(value));
        return mongoTemplate.findOne(query, clazz);
    }

    public T findOneByRegex(String key, String value) {
        Query query = new Query(Criteria.where(key).regex(value));
        return mongoTemplate.findOne(query, clazz);
    }

    public List<T> findListByRegex(String key, String value) {
        Query query = new Query(Criteria.where(key).regex(value));
        return mongoTemplate.find(query, clazz);
    }

    public void updateById(T t) {
        try {
            Field idField = clazz.getDeclaredField("id");
            idField.setAccessible(true);
            Query query = new Query(Criteria.where("_id").is(idField.get(t)));
            Update update = new Update();
            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);
                Object value = field.get(t);
                if (Objects.nonNull(value)) {
                    update.set(field.getName(), value);
                }
            }
            mongoTemplate.updateFirst(query, update, clazz);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<T> findByRegex(String key, String regex) {
        Query query = new Query(Criteria.where(key).regex(regex));
        return mongoTemplate.find(query, clazz);
    }

    public void insert(T t) {
        mongoTemplate.insert(t);
    }

    public void removeById(String id) {
        Query query = new Query(Criteria.where("id").is(id));
        mongoTemplate.remove(query, clazz);
    }

    public MongoCollection<Document> getMongoCollection(){
        return mongoTemplate.getCollection(mongoTemplate.getCollectionName(clazz));
    }
}
