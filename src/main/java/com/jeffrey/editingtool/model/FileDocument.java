package com.jeffrey.editingtool.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.File;
import java.util.Date;
import java.util.Map;

/**
 * @author jeffreydou
 */
@Data
@Document(collection="t_file_document")
public class FileDocument {
    @Id  // 主键
    private String id;
    @Field("name")
    private String name;        // 文件名称
    @Field("size")
    private Long size;          // 文件大小
    @Field("upload_date")
    private Date uploadDate;    // 上传时间
    @Field("md5")
    private String md5;         // 文件MD5值
    @Field("content_type")
    private String contentType; // 文件类型
    @Field("suffix")
    private String suffix;      // 文件后缀名
    @Field("grid_fs_id")
    private String gridFsId;    // 大文件管理GridFS的ID
    @Field("offset")
    private Integer offset; //时间偏移
    @Field("z_index")
    private Integer zIndex; //层级
    @Field("start_time")
    private Long startTime; //显示开始时间
    @Field("end_time")
    private Long endTime; //显示结束时间
    @Field("duration")
    private Long duration; //视频时长
    @Field("x_coordinate")
    private Integer xCoordinate; //x坐标平移量
    @Field("y_coordinate")
    private Integer yCoordinate; //y坐标平移量
//    @Field("magnification")
//    private Integer magnification; //放大缩小比例
    @Transient
    private byte[] content;     // 文件内容
    @Transient
    private Map<String, Object> metadata; //元数据
    @Transient
    private File file; //文件
    @Transient
    private String description; // 文件描述
    @Field("width")
    private Integer width;
    @Field("height")
    private Integer height;
    @Field("volume")
    private Integer volume;
    @Transient
    private String filePath;
}
