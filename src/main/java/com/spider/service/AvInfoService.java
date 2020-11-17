package com.spider.service;
import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCursor;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import com.spider.entity.AvInfo;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@Service
public class AvInfoService extends  BaseService<AvInfo>{

    public static List<String> codeList=new ArrayList<>();

    public List<AvInfo> findByAvatar(String avatar){
        Query query=new Query(Criteria.where("avatars").is(avatar));
        return mongoTemplate.find(query,clazz);
    }

    @PostConstruct
    public void initializeCodeList(){
        Query query=new Query();

        //MongoCursor<Document> data= mongoTemplate.getCollection(mongoTemplate.getCollectionName(clazz)).find().projection(new BasicDBObject("code",1)).iterator();
    }

}
