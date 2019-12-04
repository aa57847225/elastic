package com.whl.demo;

import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
//import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import static org.elasticsearch.common.xcontent.XContentFactory.*;


import java.net.InetAddress;
import java.util.Date;

public class Test {

    /**
     * 使用es的帮助类
     */
    public static XContentBuilder createJson4() throws Exception {
        // 创建json对象, 其中一个创建json的方式
        XContentBuilder source = XContentFactory.jsonBuilder()
                .startObject()
                .field("user", "kimchy")
                .field("postDate", new Date())
                .field("message", "trying to out ElasticSearch")
                .endObject();
        return source;
    }

    public static void main(String[] args) throws Exception {
//        Client client = new TransportClient()
//                .addTransportAddress(new InetSocketTransportAddress(
//                        "192.168.0.70",
//                        9300));
//        XContentBuilder source = Test.createJson4();
//        IndexResponse response = client.prepareIndex("twitter", "tweet", "1").setSource(source).get();
//        String index = response.getIndex();
//        String type = response.getType();
//        String id = response.getId();
//        long version = response.getVersion();
//        boolean created = response.isCreated();
//        System.out.println(index + " : " + type + ": " + id + ": " + version + ": " + created);
        Settings settings = Settings.builder().put("cluster.name", "elasticsearch").build();
        TransportClient client = new PreBuiltTransportClient(Settings.EMPTY)
                .addTransportAddress(new TransportAddress(InetAddress.getByName("192.168.0.70"), 9300));
//                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("192.168.0.70"), 9300));

        XContentBuilder builder = jsonBuilder()
                .startObject()
                .field("user", "kimchy")
                .field("postDate", new Date())
                .field("message", "trying out Elasticsearch")
                .endObject();
        String json = builder.string();

        IndexResponse response = client.prepareIndex("twitter", "tweet", "1")
                .setSource(jsonBuilder()
                        .startObject()
                        .field("user", "kimchy")
                        .field("postDate", new Date())
                        .field("message", "trying out Elasticsearch")
                        .endObject()
                )
                .get();
        // Index name
        String _index = response.getIndex();
        // Type name
        String _type = response.getType();
        // Document ID (generated or not)
        String _id = response.getId();
        // Version (if it's the first time you index this document, you will get: 1)
        long _version = response.getVersion();
        // status has stored current instance statement.
        RestStatus status = response.status();

        System.out.println(_index);
        System.out.println(_type);
        System.out.println(_id);
        System.out.println(_version);
        System.out.println(status);
    }
}