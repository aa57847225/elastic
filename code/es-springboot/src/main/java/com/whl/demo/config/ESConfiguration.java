package com.whl.demo.config;

import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.net.InetAddress;

/**
 * 数据配置，进行初始化操作
 * 
 * @author sdc
 *
 */
@Configuration
public class ESConfiguration implements FactoryBean<TransportClient>, InitializingBean, DisposableBean {
	
	private static final Logger logger = LoggerFactory.getLogger(ESConfiguration.class);
	
	/**
	 * es集群地址
	 */
	@Value("${elasticsearch.ip}")
	private String hostName;
	/**
	 * 端口
	 */
	@Value("${elasticsearch.port}")
	private String port;
	/**
	 * 集群名称
	 */
	@Value("${elasticsearch.cluster.name}")
	private String clusterName;
	
	/**
	 * 连接池
	 */
	@Value("${elasticsearch.pool}")
	private String poolSize;

	/**
	 * 队列大小
	 */
	@Value("${elasticsearch.queue}")
	private String queueSize;
	
	private TransportClient client;

	@Override
	public void destroy() throws Exception {
		try {
			logger.info("Closing elasticSearch client");
			if (client != null) {
				client.close();
			}
		} catch (final Exception e) {
			logger.error("Error closing ElasticSearch client: ", e);
		}
	}

	@Override
	public TransportClient getObject() throws Exception {
		return client;
	}

	@Override
	public Class<TransportClient> getObjectType() {
		return TransportClient.class;
	}

	@Override
	public boolean isSingleton() {
		return false;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		try {
			// 配置信息
			/**
			 * 线程池的种类
			 * cache：这是无限制的线程池，为每个传入的请求创建一个线程。
			 * fixed：这是一个有着固定大小的线程池，大小由size属性指定，允许你指定一个队列（使用queue_size属性指定）用来保存请求，直到有一个空闲的线程来执行请求。
			 * 如果Elasticsearch无法把请求放到队列中（队列满了），该请求将被拒绝。有很多线程池（可以使用type属性指定要配置的线程类型），然而，对于性能来说，最重要的是下面几个。
			 *
			 index：此线程池用于索引和删除操作。它的类型默认为fixed，size默认为可用处理器的数量，队列的size默认为300。
			 search：此线程池用于搜索和计数请求。它的类型默认为fixed，size默认为可用处理器的数量乘以3，队列的size默认为1000。
			 suggest：此线程池用于建议器请求。它的类型默认为fixed，size默认为可用处理器的数量，队列的size默认为1000。
			 get：此线程池用于实时的GET请求。它的类型默认为fixed，size默认为可用处理器的数量，队列的size默认为1000。
			 bulk：你可以猜到，此线程池用于批量操作。它的类型默认为fixed，size默认为可用处理器的数量，队列的size默认为50。
			 percolate：此线程池用于预匹配器操作。它的类型默认为fixed，size默认为可用处理器的数量，队列的size默认为1000。

			 如:
			 threadpool.index.type: fixed
			 threadpool.index.size: 100
			 threadpool.index.queue_size: 500
			 "threadpool.index.type": "fixed",
			 "threadpool.index.size": 100,
			 "threadpool.index.queue_size": 500
			 *
			 */
			Settings esSetting = Settings.builder().put("cluster.name", clusterName).put("client.transport.sniff", true)// 增加嗅探机制，找到ES集群
//					.put("thread_pool.search.type", "fixed")
					.put("thread_pool.search.size", Integer.parseInt(poolSize))// 增加线程池个数，暂时设为500
//					.put("thread_pool.bulk.size", 50)
					.build();

			client = new PreBuiltTransportClient(esSetting);
			InetSocketTransportAddress inetSocketTransportAddress = new InetSocketTransportAddress(InetAddress.getByName(hostName), Integer.valueOf(port));
			client.addTransportAddresses(inetSocketTransportAddress);

		} catch (Exception e) {
			logger.error("elasticsearch TransportClient create error!!!", e);
		}
	}

}
