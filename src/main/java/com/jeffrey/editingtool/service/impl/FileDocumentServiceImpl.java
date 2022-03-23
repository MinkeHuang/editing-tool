package com.jeffrey.editingtool.service.impl;

import com.jeffrey.editingtool.model.FileDocument;
import com.jeffrey.editingtool.service.FileDocumentService;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.mongodb.client.result.DeleteResult;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.IdUtil;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.util.Assert;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author jeffreydou
 */
@Service
public class FileDocumentServiceImpl implements FileDocumentService {
    @Autowired
    GridFsTemplate gridFsTemplate;
    @Autowired
    MongoTemplate mongoTemplate;

    @Autowired
    GridFSBucket fsBucket;

    @Override
    public FileDocument findOneByMD5(String md5) {
        Query query=new Query(Criteria.where("md5").is(md5));
        return mongoTemplate.findOne(query, FileDocument.class);
    }

    @Override
    public FileDocument findOneById(String id) {
        Query query=new Query(Criteria.where("id").is(id));
        return mongoTemplate.findOne(query, FileDocument.class);
    }

    @Override
    public FileDocument savaFile(InputStream in, FileDocument fileModel) {
        //设置存入GridFs中的文件名
        FileDocument fileDocument = findOneByMD5(fileModel.getMd5());
        //将文件二进制数据存入GridFs中
        //需要传递输入流，文件id，文件类型
        if (fileDocument == null) {
            String gridFsId = IdUtil.simpleUUID();
            BasicDBObjectBuilder builder = new BasicDBObjectBuilder();
            Map<String, Object> metadata = fileModel.getMetadata();
            for(String key : metadata.keySet()) {
                builder.add(key, metadata.get(key));
            }
            gridFsTemplate.store(in, gridFsId, fileModel.getSuffix(), builder.get());
            fileModel.setGridFsId(gridFsId);
            //在这里将文件对象数据存入monggdb当中
            fileModel = mongoTemplate.save(fileModel);
            //在将文件id和文件名称返回给前端
            return fileModel;
            //先存储文件对象，在存储文件内容
        } else {
            //如已存在，则不保存
            return null;
        }
    }


    @Override
    public Optional<FileDocument> getFileContent(String fileId) {
        //根据id将文件对象数据从mongodb中查询出来
        FileDocument testFileModel = mongoTemplate.findById(fileId, FileDocument.class);
        //随手进行非空判断
        if (testFileModel != null) {
            //因为实际上真正的数据是存储到gridFs当中的，通过文件对象中的GridFsId就能操作gridFs了
            Query gridQuery = new Query().addCriteria(Criteria.where("filename").is(testFileModel.getGridFsId()));
            try {
                //根据id查询单挑数据
                final GridFSFile fsFile = gridFsTemplate.findOne(gridQuery);
                //打开流下载对象
                assert fsFile != null;
                GridFSDownloadStream in = fsBucket.openDownloadStream(fsFile.getObjectId());
                //在随手判断一手长度>0
                if (in.getGridFSFile().getLength() > 0) {
                    //获取流对象
                    GridFsResource resource = new GridFsResource(fsFile, in);
                    //获取数据
                    testFileModel.setContent(IoUtil.readBytes(resource.getInputStream()));
                    return Optional.of(testFileModel);
                } else {
                    return Optional.empty();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return Optional.empty();
        //先查出文件对象，在查出文件内容，在转换成流对象传递
    }

    @Override
    public List<FileDocument> listFileDocuments() {
        Query query = new Query();
        query.with(new Sort(Sort.Direction.ASC,"z_index"));
        query.with(new Sort(Sort.Direction.ASC,"upload_date"));
        return mongoTemplate.find(query, FileDocument.class);
    }

    @Override
    public long updateFileDocument(FileDocument fileDocument) {
        Assert.notNull(fileDocument.getId(), "id不能为空");
        Query query=new Query(Criteria.where("id").is(fileDocument.getId()));
        Update updateOperations= new Update();
        for(Field field : FileDocument.class.getDeclaredFields()) {
            try {
                if(field.isAnnotationPresent(org.springframework.data.mongodb.core.mapping.Field.class)) {
                    field.setAccessible(true);
                    Object value = field.get(fileDocument);
                    if (value != null) updateOperations.set(field.getAnnotation(org.springframework.data.mongodb.core.mapping.Field.class).value(), value);
                }
            } catch (IllegalArgumentException | IllegalAccessException e) {
                System.out.println("参数获取异常");
                return 0;
            }

        }
        //更新查询返回结果集的第一条
        return mongoTemplate.updateFirst(query,updateOperations, FileDocument.class).getModifiedCount();

    }

    @Override
    public int removeFile(String fileId) {
        Assert.notNull(fileId, "id不能为空");
        //先从mongodb中查询出对象数据，因为里面有我们需要的GridFs的id
        FileDocument fileModel = mongoTemplate.findById(fileId, FileDocument.class);
        if (fileModel != null) {
            //根据对象中的gridfsid操作gridfs
            Query deleteFile = new Query().addCriteria(Criteria.where("filename").is(fileModel.getGridFsId()));
            //删除文件内容
            gridFsTemplate.delete(deleteFile);
            //删除文件对象
            Query deleteQuery = new Query(Criteria.where("id").is(fileId));
            DeleteResult remove = mongoTemplate.remove(deleteQuery, FileDocument.class);
            return (int) remove.getDeletedCount();
            //先查询出 gridfsid 根据gridfsid删除gridfs中的文件内容，然后在删除mongodb中的文件对象。
        }
        return 0;
    }

    @Override public long count(String projectId) {
        return mongoTemplate.count(new Query(), FileDocument.class);
    }
}
