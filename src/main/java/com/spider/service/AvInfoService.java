package com.spider.service;
import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCursor;
import org.bson.Document;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import com.spider.entity.AvInfo;
import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AvInfoService extends  BaseService<AvInfo>{

    public static List<String> codeList=new ArrayList<>();

    public static Map<String,List<String>> codeTransformMap=new HashMap<>();

    public List<AvInfo> findByAvatar(String avatar){
        Query query=new Query(Criteria.where("avatars").is(avatar));
        return mongoTemplate.find(query,clazz);
    }

    @PostConstruct
    public void initializeCodeList(){
        MongoCursor<Document> data= mongoTemplate.getCollection(mongoTemplate.getCollectionName(clazz)).find().projection(new BasicDBObject("code",1)).iterator();
        while (data.hasNext()){
            codeList.add(data.next().getString("code"));
        }
        codeList.forEach(code->{
            List<String> list=new ArrayList<>();
            list.add(code);
            list.add(code.trim());
            list.add(code.toLowerCase());
            list.add(code.toUpperCase());
            list.add(code.replace("-",""));
            list.add(code.replace("-","_"));
            list.add(code.replace("_",""));
            list.add(code.replace("_","-"));

            list.add(code.replace("-","").toLowerCase());
            list.add(code.replace("-","_").toLowerCase());
            list.add(code.replace("_","").toLowerCase());
            list.add(code.replace("_","-").toLowerCase());

            list.add(code.replace("-","").toUpperCase());
            list.add(code.replace("-","_").toUpperCase());
            list.add(code.replace("_","").toUpperCase());
            list.add(code.replace("_","-").toUpperCase());
            List<String> mp4=  list.stream().map(c->c+".mp4").collect(Collectors.toList());
            List<String> avi= list.stream().map(c->c+".avi").collect(Collectors.toList());
            list.addAll(mp4);
            list.addAll(avi);
            codeTransformMap.put(code,list.stream().distinct().collect(Collectors.toList()));
        });
    }

}
