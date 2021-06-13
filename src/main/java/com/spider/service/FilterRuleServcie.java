package com.spider.service;

import cn.hutool.core.collection.CollectionUtil;
import com.spider.entity.FilterRule;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class FilterRuleServcie extends BaseService<FilterRule> {

    public static List<FilterRule> filterRuleList = new CopyOnWriteArrayList<>();

    @PostConstruct
    public void initFilterRuleList() {
        filterRuleList.addAll(findAll());
    }

    @Override
    public void deleteKey(Object id, String key) {
        super.deleteKey(id, key);
        filterRuleList.clear();
        filterRuleList.addAll(findAll());
    }

    @Override
    public void updateById(FilterRule filterRule) {
        super.updateById(filterRule);
        filterRuleList.clear();
        filterRuleList.addAll(findAll());
    }

    @Override
    public void insert(FilterRule filterRule) {
        Query query = new Query(Criteria.where("rule").is(filterRule.getRule()).and("type").is(filterRule.getType()));
        if (CollectionUtil.isNotEmpty(find(query))) {
            return;
        }
        super.insert(filterRule);
        filterRuleList.clear();
        filterRuleList.addAll(findAll());
    }
}
