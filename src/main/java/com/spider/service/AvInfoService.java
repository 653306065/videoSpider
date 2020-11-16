package com.spider.service;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.spider.entity.AvInfo;

import java.util.List;

@Service
public class AvInfoService extends  BaseService<AvInfo>{

    public List<AvInfo> findByAvatar(String avatar){
        Query query=new Query(Criteria.where("avatars").is(avatar));
        return mongoTemplate.find(query,clazz);
    }

}
