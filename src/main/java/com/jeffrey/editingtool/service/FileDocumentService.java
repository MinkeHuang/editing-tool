package com.jeffrey.editingtool.service;

import com.jeffrey.editingtool.model.FileDocument;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;

/**
 * @author jeffreydou
 */
public interface FileDocumentService {
    /**
     * @param in        io输入流
     * @param fileModel 对应文件的模型类
     * @return 成功
     */
    FileDocument savaFile(InputStream in, FileDocument fileModel);

    FileDocument findOneByMD5(String md5);

    FileDocument findOneById(String id);

    /**
     * 从mongodb中将文件内容查询出来
     *
     * @param fileId 文件id
     * @return Optional<TestFileModel>
     */
    Optional<FileDocument> getFileContent(String fileId);

    /**
     * 删除mongodb  根据file_id
     *
     * @param fileId
     * @return 1 success
     */
    int removeFile(String fileId);

    /**
     * 只更新基本信息，不更新绑定文件
     * @param fileDocument
     * @return
     */
    long updateFileDocument(FileDocument fileDocument);

    /**
     * zindex正序，创建时间正序
     */
    List<FileDocument> listFileDocuments();

    /**
     * @param projectId //TODO工程id筛选
     * @return
     */
    long count(String projectId);
}
