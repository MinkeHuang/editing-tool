package com.jeffrey.editingtool.controller;

import com.jeffrey.editingtool.Constants.AbstractConstants;
import com.jeffrey.editingtool.bean.ResultBean;
import com.jeffrey.editingtool.model.FileDocument;
import com.jeffrey.editingtool.service.FileDocumentService;
import com.jeffrey.editingtool.utils.ToolUtil;
import com.jeffrey.editingtool.vo.FileDocumentVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author jeffreydou
 */
@RestController
@RequestMapping("filedocument")
public class FileDocumentController {
    @Autowired FileDocumentService fileDocumentService;

    @RequestMapping("list")
    @ResponseBody
    public List<FileDocument> list() {
        return fileDocumentService.listFileDocuments();
    }

    @PostMapping("upload")
    @ResponseBody
    public ResultBean upload(FileDocumentVO vo) throws IOException, NoSuchAlgorithmException {
        if (StringUtils.isEmpty(vo.getFilePath()))
            return new ResultBean(AbstractConstants.CODE_ILLEGAL, "上传失败，参数错误");
        File file = new File(vo.getFilePath());
        if(!file.exists())
            return new ResultBean(AbstractConstants.CODE_ILLEGAL, "上传失败，文件不存在");
        FileDocument fileDocument = new FileDocument();
        fileDocument.setMd5(ToolUtil.getMD5(new FileInputStream(file)));
        Map<String, Object> map = ToolUtil.getMetadata(vo.getFilePath());
        long duration = ToolUtil.getTimelen((String)map.get("duration"));
        fileDocument.setMetadata(map);
        String suffix = file.getName().substring(file.getName().lastIndexOf(".") + 1);
        fileDocument.setDuration(duration);
        fileDocument.setStartTime(0l);
        fileDocument.setEndTime(duration);
        fileDocument.setSuffix(suffix);
        fileDocument.setContentType(ToolUtil.getContentTypeBySuffix(suffix));
        fileDocument.setSize(file.length());
        fileDocument.setUploadDate(new Date());
        fileDocument.setName(file.getName());
        fileDocument.setVolume(100);
        String resolution = (String)map.get("resolution");
        String[] split = resolution.split("x");
        fileDocument.setWidth(Integer.parseInt(split[0].trim()));
        fileDocument.setHeight(Integer.parseInt(split[1].trim()));
        fileDocument.setZIndex((int)fileDocumentService.count(null));
        FileDocument savedFile = fileDocumentService.savaFile(new FileInputStream(file), fileDocument);
        if (savedFile == null)
            return new ResultBean(AbstractConstants.CODE_ILLEGAL, "上传失败，此文件已上传");
        return new ResultBean(AbstractConstants.CODE_OK, savedFile, "上传成功");
    }

    @PostMapping("delete")
    @ResponseBody
    public ResultBean delete(FileDocumentVO vo) {
        if (vo.getFileDocument() == null || StringUtils.isEmpty(vo.getFileDocument().getId()))
            return new ResultBean(AbstractConstants.CODE_ILLEGAL, "删除失败，参数错误");
        int rowsAffected = fileDocumentService.removeFile(vo.getFileDocument().getId());
        if (rowsAffected > 0)
            return new ResultBean(AbstractConstants.CODE_OK, true, "删除成功");
        return new ResultBean(AbstractConstants.CODE_ILLEGAL, "因未知原因，删除失败");
    }

    @PostMapping("update")
    @ResponseBody
    public ResultBean update(FileDocumentVO vo) {
        if (vo.getFileDocument() == null || StringUtils.isEmpty(vo.getFileDocument().getId()))
            return new ResultBean(AbstractConstants.CODE_ILLEGAL, "更新失败，参数错误");
        long rowsAffected = fileDocumentService.updateFileDocument(vo.getFileDocument());
        if (rowsAffected > 0)
            return new ResultBean(AbstractConstants.CODE_OK, true, "更新成功");
        return new ResultBean(AbstractConstants.CODE_ILLEGAL, "因未知原因，更新失败");
    }
}
