package com.spider.service.es;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.QueryStringQueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;;
import javax.annotation.PostConstruct;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.*;

public abstract class BaseElasticsearchService<T> {

    private Class<T> clazz;

    @PostConstruct
    protected void init() {
        clazz = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    @Autowired
    protected ElasticsearchRestTemplate elasticsearchRestTemplate;

    public List<SearchHit<T>> searchByKeyValue(String key, String value) {
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder().must(new MatchQueryBuilder(key, value));
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(boolQueryBuilder).build();
        return elasticsearchRestTemplate.search(searchQuery, clazz).toList();
    }

    public List<SearchHit<T>> searchByValue(String value) {
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(QueryBuilders.queryStringQuery(value)).build();
        return elasticsearchRestTemplate.search(searchQuery, clazz).toList();
    }

    public void save(T t) {
        elasticsearchRestTemplate.save(t);
    }

    public void saveList(Collection<T> collection) {
        elasticsearchRestTemplate.save(collection);
    }

    public void delete(T t) {
        elasticsearchRestTemplate.delete(t);
    }

    public void deleteById(String id) {
        elasticsearchRestTemplate.delete(id, clazz);
    }

    public void updateById(T t) {
        try {
            Field id = clazz.getDeclaredField("id");
            id.setAccessible(true);
            String idStr = String.valueOf(id.get(t));
            deleteById(idStr);
            save(t);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
