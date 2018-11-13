package org.githup.es.dao;

import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Requests;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.mapper.Mapping;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Date;

/**
 * ES的操作数据类
 * 
 * 备注：对es的一些操作做了一些封装，抽出来一些操作，就是传统的dao层，数据服务
 * 
 * @author sdc
 *
 */
@Component
public class BaseRepository {

	private static final Logger LOG = LoggerFactory.getLogger(BaseRepository.class);

	@Autowired
	private TransportClient client;
	
	/**
	 * 创建索引
	 *
	 * @param index
	 * @return
	 */
	public boolean buildIndex(String index,String type) {
		if (!isIndexExist(index)) {
			LOG.info("Index is not exits!");
		}
		CreateIndexResponse buildIndexresponse = client.admin().indices().prepareCreate(index).execute().actionGet();
		LOG.info(" 创建索引的标志: " + buildIndexresponse.isAcknowledged());

		return buildIndexresponse.isAcknowledged();
	}

	public boolean buildPostIndex()  {
		try {
//			XContentBuilder mapping = XContentFactory.jsonBuilder()
//					.startObject()
//					.startObject("properties")
//					//      .startObject("m_id").field("type","keyword").endObject()
//					.startObject("poi_index").field("type","integer").endObject()
//					.startObject("poi_title").field("type","text").field("analyzer","ik_max_word").endObject()
//					.startObject("poi_address").field("type","text").field("analyzer","ik_max_word").endObject()
//					.startObject("poi_tags").field("type","text").field("analyzer","ik_max_word").endObject()
//					.startObject("poi_phone").field("type","text").field("analyzer","ik_max_word").endObject()
//					.startObject("poi_lng").field("type","text").endObject()
//					.startObject("poi_lat").field("type","text").endObject()
//					.endObject()
//					.endObject();

			XContentBuilder mapping = XContentFactory.jsonBuilder()
					.startObject()
					.startObject("properties")
					//      .startObject("m_id").field("type","keyword").endObject()
					.startObject("collectCount").field("type","integer").endObject()
					.startObject("commentCount").field("type","integer").endObject()
					.startObject("content").field("type","text").endObject()
					.startObject("createTime").field("type","date").endObject()
					.startObject("downloadCount").field("type","integer").endObject()
					.startObject("forwardCount").field("type","integer").endObject()
					.startObject("id").field("type","integer").endObject()
					.startObject("isAnonymity").field("type","integer").endObject()
					.startObject("originalContent").field("type","text").endObject()
					.startObject("postVariety").field("type","integer").endObject()
					.startObject("readCount").field("type","integer").endObject()
					.startObject("type").field("type","integer").endObject()
					.startObject("updateTime").field("type","date").endObject()
					.startObject("userId").field("type","integer").endObject()
					.startObject("valid").field("type","integer").endObject()
					.startObject("suggest").field("type","completion").endObject()
					.endObject()
					.endObject();
			//pois：索引名   cxyword：类型名（可以自己定义）
			PutMappingRequest putmap = Requests.putMappingRequest("bbs_post_index").type("post").source(mapping);
			//创建索引
			client.admin().indices().prepareCreate("bbs_post_index").execute().actionGet();
			//为索引添加映射
			client.admin().indices().putMapping(putmap).actionGet();
		}catch (Exception e){

		}
		return true;
	}

	/**
	 * 增加文档，测试用的- 增加文档
	 * 
	 * @param
	 * @return
	 * @throws Exception
	 */
	public int addPostDataDoc(String postId, String postContent) throws Exception {
		IndexResponse response = client.prepareIndex("forum_index", "post").setSource(XContentFactory.jsonBuilder().startObject().field("id", postId).field("content", postContent).endObject()).get();
		return response.hashCode();
	}

	/**
	 * 删除索引
	 *
	 * @param index
	 * @return
	 */
	public boolean deleteIndex(String index) {
		if (!isIndexExist(index)) {
			LOG.info(" 索引不存在 ！！！！！!");
		}
		DeleteIndexResponse diResponse = client.admin().indices().prepareDelete(index).execute().actionGet();
		if (diResponse.isAcknowledged()) {
			LOG.info("删除索引**成功** index->>>>>>>" + index);
		} else {
			LOG.info("删除索引**失败** index->>>>> " + index);
		}
		return diResponse.isAcknowledged();
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
			LOG.info("此索引 [" + index + "] 已经在ES集群里存在");
		} else {
			LOG.info(" 没有此索引 [" + index + "] ");
		}
		return iep.isExists();
	}

	public String bulkBatch() {

		try {
			BulkRequestBuilder bulkRequest = client.prepareBulk();
			//插入
			bulkRequest.add(this.client.prepareIndex("news_website", "news", "1")
					.setSource(XContentFactory.jsonBuilder()
							.startObject()
							.field("title","大话西游电影")
							.field("content","大话西游的电影时隔20年即将在2017年4月重映")
							.endObject()
					)
			);
			bulkRequest.add(this.client.prepareIndex("news_website", "news", "2")
					.setSource(XContentFactory.jsonBuilder()
							.startObject()
							.field("title","大话西游小说")
							.field("content","某知名网络小说作家已经完成了大话西游同名小说的出版")
							.endObject()
					)
			);
			bulkRequest.add(this.client.prepareIndex("news_website", "news", "3")
					.setSource(XContentFactory.jsonBuilder()
							.startObject()
							.field("title","大话西游手游")
							.field("content","网易游戏近日出品了大话西游经典IP的手游，正在火爆内测中")
							.endObject()
					)
			);
			//批量执行
			BulkResponse bulkResponse = bulkRequest.get();
			return  bulkResponse.getTook().toString();
		}catch (Exception e){
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		return null;
	}

}
