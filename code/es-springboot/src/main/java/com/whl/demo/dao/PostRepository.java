package com.whl.demo.dao;

import com.whl.demo.module.Post;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * @program: esspringboot
 * @description:
 * @author: Mr.Wang
 * @create: 2018-11-14 09:15
 **/
public interface PostRepository extends ElasticsearchRepository<Post, String> {
}
