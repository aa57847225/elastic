package com.whl.demo.dao;

import com.alibaba.fastjson.JSON;
import com.whl.demo.constants.IndexCommon;
import com.whl.demo.module.Book;
import com.whl.demo.module.Person;
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

@Component
public class PersonRepository {

    private static final Logger logger = LoggerFactory.getLogger(PersonRepository.class);

    @Autowired
    private TransportClient client;

    /**
     * 构建人员索引
     * 拼音+IK
     * @return
     */
    public boolean buildPersonIndex() {
        try {

            XContentBuilder mapping2 = XContentFactory.jsonBuilder()
                    .startObject()
                        .startObject("index")
                            .startObject("analysis")
                                .startObject("analyzer")
                                    .startObject("ik_pinyin_analyzer")
                                        .field("type","custom")
                                        .field("tokenizer","ik_smart")
                                        .field("filter","pinyin_filter")
                                    .endObject()
                                .endObject()
                                .startObject("filter")
                                    .startObject("pinyin_filter")
                                        .field("type","pinyin")
                                        .field("keep_first_letter",false)
                                    .endObject()
                                .endObject()
                            .endObject()
                        .endObject()
                    .endObject();

            XContentBuilder mapping = XContentFactory.jsonBuilder()
                    .startObject()
                        .startObject("properties")
                            .startObject("id").field("type", "string").endObject()
                            .startObject("name").field("type", "string")
                                .field("analyzer","ik_smart")
                                .field("search_analyzer","ik_smart")
                                    .startObject("fields").startObject("my_pinyin")
                                        .field("type","text")
                                        .field("analyzer","ik_pinyin_analyzer")
                                        .field("search_analyzer","ik_pinyin_analyzer")
                                    .endObject().endObject()
                                .endObject()
                        .endObject()
                    .endObject();
            //pois：索引名   cxyword：类型名（可以自己定义）
            PutMappingRequest putmap = Requests.putMappingRequest(IndexCommon.index_person).type(IndexCommon.index_person_type_xwc).source(mapping);
            //创建索引
            client.admin().indices().prepareCreate(IndexCommon.index_person).setSettings(mapping2).execute().actionGet();
            //为索引添加映射
            PutMappingResponse buildIndexresponse = client.admin().indices().putMapping(putmap).actionGet();
            return buildIndexresponse.isAcknowledged();
        } catch (Exception e) {
            logger.info("== build person Index error ==" + e.getMessage(), e);
        }
        return false;
    }

    /**
     * 添加单个人员数据
     * @param person
     * @return
     */
    public String addPersonIndexDataSingle(Person person) {
        try {
            BulkRequestBuilder bulkRequest = client.prepareBulk();
            //插入
            bulkRequest.add(this.client.prepareIndex(IndexCommon.index_person, IndexCommon.index_person_type_xwc, person.getId())
                    .setSource(XContentFactory.jsonBuilder()
                            .startObject()
                            .field("id", person.getId())
                            .field("name", person.getName())
                            .field("createTime", person.getCreateTime())
                            .field("age", person.getAge())
                            .endObject()
                    )
            );
            //批量执行
            BulkResponse bulkResponse = bulkRequest.get();
            return bulkResponse.getTook().toString();
        } catch (Exception e) {
            logger.info(e.getMessage(),e);
        }
        return null;
    }

    /**
     * 批量添加人员数据
     * @return
     */
    public String addPersonIndexDataBatch(List<Person> personList) {

        try {
            BulkRequestBuilder bulkRequest = client.prepareBulk();

            for (Person person : personList) {
                //插入
                bulkRequest.add(this.client.prepareIndex(IndexCommon.index_person, IndexCommon.index_person_type_xwc, person.getId())
                        .setSource(XContentFactory.jsonBuilder()
                                .startObject()
                                .field("id", person.getId())
                                .field("name", person.getName())
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
     * 更新单人员数据
     * @param person
     * @return
     */
    public String updatePersonIndexDataSingle(Person person) {
        try {
            XContentBuilder builder = XContentFactory.jsonBuilder().startObject()
                    .field("id", person.getId())
                    .field("name", person.getName())
                    .field("createTime", person.getCreateTime())
                    .field("age", person.getAge())
                    .endObject();
            UpdateResponse response = client.prepareUpdate(IndexCommon.index_person, IndexCommon.index_person_type_xwc, person.getId()).setDoc(builder).get();
            return response.status().toString();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 批量更新人员数据
     *
     * @param personList
     * @return
     */
    public boolean updatePersonIndexDataBatch(List<Person> personList) {

        boolean isUpdate = false;
        try {
            BulkRequestBuilder bulkRequest = client.prepareBulk();
            for(Person person:personList){
                bulkRequest.add(client.prepareUpdate(IndexCommon.index_person, IndexCommon.index_person_type_xwc, person.getId()).setDoc(XContentFactory.jsonBuilder()
                        .startObject()
                        .field("id", person.getId())
                        .field("name", person.getName())
                        .field("createTime", person.getCreateTime())
                        .field("age", person.getAge())
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

    /**
     * 判断索引是否存在
     *
     * @param index
     * @return
     */
    public boolean isIndexExist(String index) {
        IndicesExistsResponse iep = client.admin().indices().exists(new IndicesExistsRequest(index)).actionGet();
        if (iep.isExists()) {
            logger.info("此索引 [" + index + "] 已经在ES集群里存在");
        } else {
            logger.info(" 没有此索引 [" + index + "] ");
        }
        return iep.isExists();
    }
}
