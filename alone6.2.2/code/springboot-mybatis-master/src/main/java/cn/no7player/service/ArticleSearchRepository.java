package cn.no7player.service;

import cn.no7player.model.Article;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Article service
 */
public interface ArticleSearchRepository extends ElasticsearchRepository<Article, Long> {
}
