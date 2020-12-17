package com.spider.dao.es;

import com.spider.entity.es.EsVideo;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.data.elasticsearch.repository.ReactiveElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ESVideoDao extends ElasticsearchRepository<EsVideo,String>   {

    List<EsVideo> findByName(String value);
}
