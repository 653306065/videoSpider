package com.spider.service.es;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.query.StringQuery;

import javax.annotation.PostConstruct;
import java.lang.reflect.ParameterizedType;
import java.util.List;


public abstract class BaseElasticsearchsService<T> {

    private Class<T> clazz;

    @PostConstruct
    public void init() {
        clazz = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    @Autowired
    protected ElasticsearchRestTemplate elasticsearchRestTemplate;

    public List<SearchHit<T>> searchByValue(String value) {
        StringQuery stringQuery = new StringQuery(value);
        return elasticsearchRestTemplate.search(stringQuery, clazz).toList();
    }
}
