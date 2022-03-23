package com.jeffrey.editingtool.vo;

import com.jeffrey.editingtool.model.FileDocument;
import lombok.Data;

/**
 * @author jeffreydou
 */
@Data
public class FileDocumentVO {
    private String filePath;
    private FileDocument fileDocument;
    public FileDocumentVO(FileDocument fileDocument) {
        this.fileDocument = fileDocument;
    }
    public FileDocumentVO() {
    }
}
