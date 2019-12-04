//package com.whl.demo;
//
//import java.util.Date;
//
//public class Application {
//
//    public static XContentBuilder createJson4() throws Exception {
//        // 创建json对象, 其中一个创建json的方式
//        XContentBuilder source = XContentFactory.jsonBuilder()
//                .startObject()
//                .field("user", "kimchy")
//                .field("postDate", new Date())
//                .field("message", "trying to out ElasticSearch")
//                .endObject();
//        return source;
//    }
//
//    public static void main(String[] args) throws Exception {
//        Client client = new TransportClient()
//                .addTransportAddress(new InetSocketTransportAddress(
//                        "192.168.0.66",
//                        9300));
//        XContentBuilder source = Application.createJson4();
//        IndexResponse response = client.prepareIndex("twitter", "tweet", "1").setSource(source).get();
//        String index = response.getIndex();
//        String type = response.getType();
//        String id = response.getId();
//        long version = response.getVersion();
//        boolean created = response.isCreated();
//        System.out.println(index + " : " + type + ": " + id + ": " + version + ": " + created);
//    }
//}
