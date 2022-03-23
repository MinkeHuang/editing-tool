package com.jeffrey.editingtool.utils;

import com.jeffrey.editingtool.model.FileDocument;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.List;

/**
 * @author jeffreydou
 */
public class FFmpegUtil {
    public static void genInputCommands(List<String> commands, FileDocument fileDocument) {
        if (fileDocument.getStartTime() != null) {
            commands.add("-ss");
            commands.add(ToolUtil.longToTime(fileDocument.getStartTime()));
        }
        commands.add("-i");
        commands.add(fileDocument.getFilePath());
        if (fileDocument.getEndTime() != null) {
            commands.add("-t");
            commands.add(String.valueOf(fileDocument.getEndTime() - fileDocument.getStartTime()));
        }
        if (fileDocument.getOffset() != null) {
            commands.add("-itsoffset");
            commands.add(fileDocument.getOffset()+"");
        }
    }
    //根据存储的FileDocument生成视频
    public static void genVideo(String inputPath, String outputPath, FileDocument fileDocument) {
        List<String> commands = new java.util.ArrayList<>();
        commands.add("ffmpeg");
        //开始时间
        if (fileDocument.getStartTime() != null) {
            commands.add("-ss");
            commands.add(ToolUtil.longToTime(fileDocument.getStartTime()));
        }
        commands.add("-i");
        commands.add(inputPath);
        //结束时间
        if (fileDocument.getEndTime() != null) {
            commands.add("-t");
            commands.add(String.valueOf(fileDocument.getEndTime() - fileDocument.getStartTime()));
        }
        //音量调节
        if (fileDocument.getVolume() != null) {
            commands.add("-af");
            commands.add("volume="+(fileDocument.getVolume()/100));
        }
        //分辨率调整
        if (fileDocument.getWidth() != null) {
            commands.add("-s");
            commands.add(fileDocument.getWidth() + "x" + fileDocument.getHeight());
        }
        commands.add(outputPath);
        try {
            ProcessBuilder builder = new ProcessBuilder();
            Process process = builder.command(commands).redirectErrorStream(true).start();
            process.waitFor();
            process.destroy();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void pureColorPictures(int width,int height, String filePath) {
        //width 生成图宽度
        // height 生成图高度
        //创建一个width xheight ，RGB高彩图，类型可自定
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        //取得图形
        Graphics g = img.getGraphics();
        //设置颜色
        g.setColor(Color.BLACK);
        //填充
        g.fillRect(0, 0, img.getWidth(), img.getHeight());
        //在d盘创建个文件
        File file = new File(filePath);
        try {
            //以png方式写入，可改成jpg其他图片
            ImageIO.write(img, "jpg", file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //截图
    public static boolean snapshot(String videoPath, String outputPath) {
        List<String> commands = new java.util.ArrayList<>();
        commands.add("ffmpeg");
        commands.add("-ss");
        commands.add("00:00:00");
        commands.add("-i");
        commands.add(videoPath);
        commands.add("-vframes");
        commands.add("1");
        commands.add("-q:v");
        commands.add("1");
        commands.add(outputPath);
        try {
            ProcessBuilder builder = new ProcessBuilder();
            Process process = builder.command(commands).redirectErrorStream(true).start();
            process.waitFor();
            process.destroy();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
