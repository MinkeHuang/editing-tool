//package com.jeffrey.editingtool.config;
//
//import com.mongodb.MongoClientOptions;
//import com.mongodb.client.MongoClient;
//import com.mongodb.client.MongoDatabase;
//import com.mongodb.client.gridfs.GridFSBucket;
//import com.mongodb.client.gridfs.GridFSBuckets;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
//import org.springframework.data.mongodb.gridfs.GridFsTemplate;
//
//import java.util.ArrayList;
//
///**
// * @author jeffreydou
// */
//@Configuration
//public class MongoConfig {
//
//    @Value("${spring.data.mongodb.database}")
//    String db;
//
//    @Bean
//    public GridFSBucket getGridFSBucket(MongoClient mongoClient) {
//        MongoDatabase database = mongoClient.getDatabase(db);
//        GridFSBucket bucket = GridFSBuckets.create(database);
//        return bucket;
//    }
//
//}