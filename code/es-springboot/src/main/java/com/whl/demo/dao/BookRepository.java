package com.whl.demo.dao;

import com.alibaba.fastjson.JSON;
import com.whl.demo.module.Book;
import com.whl.demo.page.BootstrapTablePaginationVo;
import com.whl.demo.param.BasicSearchParam;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.Requests;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.search.suggest.Suggest;
import org.elasticsearch.search.suggest.SuggestBuilder;
import org.elasticsearch.search.suggest.completion.CompletionSuggestionBuilder;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ES的操作数据类
 * 备注：对es的一些操作做了一些封装，抽出来一些操作，就是传统的dao层，数据服务
 * @author whl
 * 只支持IK搜索
 */
@Component
public class BookRepository {

    private static final Logger logger = LoggerFactory.getLogger(BookRepository.class);

    @Autowired
    private TransportClient client;

    /**
     * 构建小说索引(暂时无分词器)
     * {
     * "mappings": {
     * "news" : {
     * "properties" : {
     * "title" : {
     * "type": "text",
     * "fields": {
     * "suggest" : {
     * "type" : "completion",
     * "analyzer": "ik_max_word"
     * }
     * }
     * },
     * "content": {
     * "type": "text",
     * "analyzer": "ik_max_word"
     * }
     * }
     * }
     * }
     * }
     *
     * @return
     */
    public boolean buildNovelBookIndex() {
        try {
//			XContentBuilder mapping = XContentFactory.jsonBuilder()
//					.startObject()
//					.startObject("properties")
//					.startObject("id").field("type","string")
//					.startObject("title").field("type","string").startObject("fields").startObject("suggest")
//					.field("type","completion").endObject().endObject().endObject()
//					.startObject("content").field("type","string").endObject()
//					.startObject("createTime").field("type","date").endObject()
//					.startObject("readCount").field("type","long").endObject()
//					.startObject("price").field("type","double").endObject()
//					.startObject("authorList").field("type","array").endObject()
//					.endObject()
//					.endObject();

//            XContentBuilder mapping2 = XContentFactory.jsonBuilder()
//                    .startObject()
//                    .startObject("index")
//                    .startObject("analysis")
//                    .startObject("analyzer")
//                    .startObject("default").field("tokenizer","ik_max_word").endObject()
//                    .startObject("pinyin_analyzer").field("tokenizer","my_pinyin").endObject()
//                    .endObject()
//                    .startObject("tokenizer").startObject("my_pinyin")
//                    .field("type","pinyin")
//                    .field("keep_first_letter",true)
//                    .field("keep_separate_first_letter",false)
//                    .field("keep_full_pinyin",false)
//                    .field("limit_first_letter_length",20)
//                    .field("lowercase",true)
//                    .field("keep_none_chinese",false)
//                    .endObject()
//                    .endObject()
//                    .endObject()
//                    .endObject()
//                    .endObject();



            XContentBuilder mapping = XContentFactory.jsonBuilder()
                    .startObject()
                    .startObject("properties")
                    .startObject("id").field("type", "string").endObject()
                    .startObject("title").field("type", "string")
                    .field("analyzer","ik_max_word").field("search_analyzer","ik_smart")
                    .startObject("fields").startObject("suggest")
                    .field("type", "completion").endObject().endObject().endObject()
                    .startObject("content").field("type", "string").field("analyzer","pinyin_analyzer").endObject()
                    .startObject("createTime").field("type", "date").field("format","yyyy-MM-dd hh:mm:ss").endObject()
                    .startObject("readCount").field("type", "long").endObject()
                    .startObject("price").field("type", "double").endObject()
                    .startObject("authorList").field("type", "string").endObject()
                    .endObject()
                    .endObject();
            //pois：索引名   cxyword：类型名（可以自己定义）
            PutMappingRequest putmap = Requests.putMappingRequest("book").type("novel").source(mapping);
            //创建索引
            client.admin().indices().prepareCreate("book").execute().actionGet();
            //为索引添加映射
            PutMappingResponse buildIndexresponse = client.admin().indices().putMapping(putmap).actionGet();
            return buildIndexresponse.isAcknowledged();
        } catch (Exception e) {
            logger.info("== buildNovelBookIndex error ==" + e.getMessage(), e);
        }
        return false;
    }


    /**
     * 添加单个小说数据
     *
     * @param book
     * @return
     */
    public String addNovelBookIndexDataSingle(Book book) {
        try {
            BulkRequestBuilder bulkRequest = client.prepareBulk();
            //插入
            bulkRequest.add(this.client.prepareIndex("book", "novel", book.getId())
                    .setSource(XContentFactory.jsonBuilder()
                            .startObject()
                            .field("id", book.getId())
                            .field("title", book.getTitle())
                            .field("content", book.getContent())
                            .field("createTime", book.getCreateTime())
                            .field("readCount", book.getReadCount())
                            .field("price", book.getPrice())
                            .field("authorList", book.getAuthorList())
                            .endObject()
                    )
            );
            //批量执行
            BulkResponse bulkResponse = bulkRequest.get();
            return bulkResponse.getTook().toString();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 批量添加小说数据
     *
     * @return
     */
    public String addNovelBookIndexDataBatch(List<Book> bookList) {

        try {
            BulkRequestBuilder bulkRequest = client.prepareBulk();

            for (Book book : bookList) {
                //插入
                bulkRequest.add(this.client.prepareIndex("book", "novel", book.getId())
                        .setSource(XContentFactory.jsonBuilder()
                                .startObject()
                                .field("id", book.getId())
                                .field("title", book.getTitle())
                                .field("content", book.getContent())
                                .field("createTime", book.getCreateTime())
                                .field("readCount", book.getReadCount())
                                .field("price", book.getPrice())
                                .field("authorList", book.getAuthorList())
                                .endObject()
                        )
                );
            }
            //批量执行
            BulkResponse bulkResponse = bulkRequest.get();
            return bulkResponse.getTook().toString();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 更新单个小说数据
     *
     * @param book
     * @return
     */
    public String updateNovelBookIndexDataSingle(Book book) {
        try {
            XContentBuilder builder = XContentFactory.jsonBuilder().startObject()
                    .field("id", book.getId())
                    .field("title", book.getTitle())
                    .field("content", book.getContent())
                    .field("createTime", book.getCreateTime())
                    .field("readCount", book.getReadCount())
                    .field("price", book.getPrice())
                    .field("authorList", book.getAuthorList())
                    .endObject();
            UpdateResponse response = client.prepareUpdate("book", "novel", book.getId()).setDoc(builder).get();

//            UpdateRequest updateRequest = new UpdateRequest();
//            updateRequest.index("book")
//                    .type("novel")
//                    .id(book.getId())
//                    .doc(XContentFactory.jsonBuilder()
//                            .startObject()
//                            .field("id", book.getId())
//                            .field("title", book.getTitle())
//                            .field("content", book.getContent())
//                            .field("createTime", book.getCreateTime())
//                            .field("readCount", book.getReadCount())
//                            .field("price", book.getPrice())
//                            .field("authorList", book.getAuthorList())
//                            .endObject()
//                    );
//            UpdateResponse response = client.update(updateRequest).get();
            return response.status().toString();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 批量更新小说数据
     *
     * @param bookList
     * @return
     */
    public boolean updateNovelBookIndexDataBatch(List<Book> bookList) {

        boolean isUpdate = false;
        try {
            BulkRequestBuilder bulkRequest = client.prepareBulk();
            for(Book book:bookList){
                bulkRequest.add(client.prepareUpdate("book", "novel", book.getId()).setDoc(XContentFactory.jsonBuilder()
                        .startObject()
                        .field("id", book.getId())
                        .field("title", book.getTitle())
                        .field("content", book.getContent())
                        .field("createTime", book.getCreateTime())
                        .field("readCount", book.getReadCount())
                        .field("price", book.getPrice())
                        .field("authorList", book.getAuthorList())
                        .endObject()));
            }
            BulkResponse bulkResponse = bulkRequest.get();
            if (bulkResponse.hasFailures()) {
                System.out.println("failures..............:" + bulkResponse.buildFailureMessage());
                return false;
            }else{
                return true;
            }
        }catch (Exception e){
            logger.info(e.getMessage(),e);
        }
        return isUpdate;
    }
}
