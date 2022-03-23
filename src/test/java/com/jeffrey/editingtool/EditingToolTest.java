package com.jeffrey.editingtool;

import com.jeffrey.editingtool.Constants.AbstractConstants;
import com.jeffrey.editingtool.bean.ResultBean;
import com.jeffrey.editingtool.controller.FileDocumentController;
import com.jeffrey.editingtool.model.FileDocument;
import com.jeffrey.editingtool.service.FileDocumentService;
import com.jeffrey.editingtool.utils.FFmpegUtil;
import com.jeffrey.editingtool.utils.ToolUtil;
import com.jeffrey.editingtool.vo.FileDocumentVO;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author jeffreydou
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class EditingToolTest {

    @Autowired FileDocumentController fileDocumentController;
    @Autowired FileDocumentService documentService;
    private List<FileDocument> list = null;

    @Test
    //点击我开始使用
    public void contextLoads() throws Exception {
        //输出说明
        System.out.println("*****欢迎使用媒体剪辑工具*****");
        String opt = "-1";
        Scanner scanner = new Scanner(System.in);
        while(!"6".equals(opt)) {
            printFileList();
            printMenu();
            System.out.println("请输入操作序号：");
            opt = scanner.nextLine();
            if ("1".equals(opt)) {
                upload();
            } else if ("2".equals(opt)) {
                edit();
            } else if ("3".equals(opt)) {
                gen();
            } else if ("4".equals(opt)) {
                delete();
            } else if ("5".equals(opt)) {
                printMetadata();
            } else if ("6".equals(opt)) {
                System.out.println("再见");
            }
        }
    }

    private void printMenu() {
        System.out.println("请选择操作：");
        System.out.println("1.添加媒体文件");
        System.out.println("2.编辑媒体信息");
        System.out.println("3.开始合成");
        System.out.println("4.删除媒体文件");
        System.out.println("5.查看媒体信息");
        System.out.println("6.退出");
    }
    private String inputFromConsole() {
        InputStreamReader in = null;
        BufferedReader br = null;
        in = new InputStreamReader(System.in);
        br = new BufferedReader(in);
        String str = "";
        try {
            str = br.readLine();
        } catch (IOException e) {
        }
        return str;
    }
    private void printFileList() {
        System.out.println("当前已添加的媒体列表：");
        //TODO 可优化前端刷新排序
        list = fileDocumentController.list();
        for(int i = 0; i < list.size(); i++) {
            FileDocument fileDocument = list.get(i);
            System.out.print("[" + (fileDocument.getZIndex() + 1) + "]" + fileDocument.getName());
            System.out.print("   " + ToolUtil.getTime(fileDocument.getDuration()));
            long numOfStar = (fileDocument.getEndTime() - fileDocument.getStartTime()) / 30 + 1;
            float numOfSpace = (fileDocument.getOffset() == null || fileDocument.getOffset() == 0) ? 0 : fileDocument.getOffset() / 30 + 1;
            System.out.println();
            for(int num = 0; num < numOfSpace; num++) {
                System.out.print(" ");
            }
            for(int num = 0; num < numOfStar; num++) {
                System.out.print("#");
            }
            System.out.println();
        }
    }

    private int checkNum(String number) {
        try {
            int num = Integer.valueOf(number);
            num -= 1;
            if (num < 0 || num >= list.size()) throw new NumberFormatException();
            return num;
        } catch (NumberFormatException e) {
            System.out.println("请输入正确序号！");
        }
        return -1;
    }

    private void upload() throws IOException, NoSuchAlgorithmException {
        System.out.println("请输入文件全路径：");
        String path = inputFromConsole();
        FileDocumentVO vo = new FileDocumentVO();
        vo.setFilePath(path);
        ResultBean resultBean  = fileDocumentController.upload(vo);
        System.out.println(resultBean.getMsg());
        if (resultBean.getCode() == AbstractConstants.CODE_OK)
            list.add((FileDocument)resultBean.getBizResult());
    }

    private void delete() {
        System.out.println("请选择需要删除的序号：");
        String number = inputFromConsole();
        int num = checkNum(number);
        if ( num > -1) {
            FileDocument fileDocument = list.get(num);
            System.out.println("正在删除"+fileDocument.getName()+"，请确认（y/n）");
            String confirm = inputFromConsole();
            if (!"y".equals(confirm)) return;
            FileDocumentVO vo = new FileDocumentVO();
            vo.setFileDocument(fileDocument);
            ResultBean resultBean = fileDocumentController.delete(vo);
            System.out.println(resultBean.getMsg());
            if (resultBean.getCode() == AbstractConstants.CODE_OK) {
                list.remove(num);
                for(int i = num; i < list.size();i++) {
                    FileDocument file = list.get(i);
                    file.setZIndex(file.getZIndex() - 1);
                    fileDocumentController.update(new FileDocumentVO(file));
                }
            }

        }
    }

    private void edit() throws IOException, NoSuchAlgorithmException {
        System.out.println("请选择编辑的序号：");
        String number = inputFromConsole();
        int index = checkNum(number);
        if (index > -1) {
            FileDocument fileDocument = list.get(index);
            System.out.println("正在编辑"+ fileDocument.getName());
            System.out.println("请选择需要编辑操作：");
            System.out.println("1.轨道平移");
            System.out.println("2.画面x轴移动");
            System.out.println("3.画面y轴移动");
            System.out.println("4.画面缩放");
            System.out.println("5.开始时间设置");
            System.out.println("6.结束时间设置");
            System.out.println("7.层级");
            System.out.println("8.音量");
            System.out.println("9.裁剪TODO");
            System.out.println("10.退出编辑");
            String opt = inputFromConsole();
            if ("1".equals(opt)) {
                //TODO 类型校验，毫秒优化
                System.out.println("请输入平移秒数（正数）：");
                Integer input = Integer.valueOf(inputFromConsole());
                if (input < 0) input = 0;
                Integer offset = fileDocument.getOffset() == null ? input : fileDocument.getOffset() + input;
                fileDocument.setOffset(offset);
                fileDocumentController.update(new FileDocumentVO(fileDocument));
            } else if ("2".equals(opt)) {
                System.out.println("请输入x轴移动像素（整数）：");
                Integer input = Integer.valueOf(inputFromConsole());
                fileDocument.setXCoordinate(input);
                fileDocumentController.update(new FileDocumentVO(fileDocument));
            } else if ("3".equals(opt)) {
                System.out.println("请输入y轴移动像素（整数）：");
                Integer input = Integer.valueOf(inputFromConsole());
                fileDocument.setYCoordinate(input);
                fileDocumentController.update(new FileDocumentVO(fileDocument));
            } else if ("4".equals(opt)) {
                System.out.println("请输入宽：");
                Integer width = Integer.valueOf(inputFromConsole());
                System.out.println("请输入高（输入-1则同宽等比例缩放）：");
                Integer height = Integer.valueOf(inputFromConsole());
                if (height == -1) {
                    fileDocument.setHeight((int)((double)width / fileDocument.getWidth() * fileDocument.getHeight()));

                } else {
                    fileDocument.setHeight(height);
                }
                fileDocument.setWidth(width);
                fileDocumentController.update(new FileDocumentVO(fileDocument));
            } else if ("5".equals(opt)) {
                System.out.println("请输入开始时间（按照hh:mm:ss格式)，当前文件时间区间：" + ToolUtil.longToTime(fileDocument.getStartTime()) + " - " + ToolUtil.longToTime(fileDocument.getEndTime()));
                String input = inputFromConsole();
                long startTime = ToolUtil.getTimelen(input);
                if (startTime > fileDocument.getDuration()) startTime = fileDocument.getDuration();
                if (startTime < 0) startTime = 0;
                fileDocument.setStartTime(startTime);
                fileDocumentController.update(new FileDocumentVO(fileDocument));
            } else if ("6".equals(opt)) {
                System.out.println("请输入结束时间（按照hh:mm:ss格式)，当前文件时间区间：" + ToolUtil.longToTime(fileDocument.getStartTime()) + " - " + ToolUtil.longToTime(fileDocument.getEndTime()));
                String input = inputFromConsole();
                long endTime = ToolUtil.getTimelen(input);
                if (endTime > fileDocument.getDuration()) endTime = fileDocument.getDuration();
                if (endTime < 0) endTime = 0;
                fileDocument.setEndTime(endTime);
                fileDocumentController.update(new FileDocumentVO(fileDocument));
            } else if ("7".equals(opt)) {
                System.out.println("请输入层级（正整数）：");
                String input = inputFromConsole();
                int zindex = Integer.parseInt(input) - 1;
                list.remove(index);
                for(int i = zindex; i < list.size(); i++ ) {
                    FileDocument file = list.get(i);
                    file.setZIndex(file.getZIndex() + 1);
                    fileDocumentController.update(new FileDocumentVO(file));
                }
                fileDocument.setZIndex(zindex);
                fileDocumentController.update(new FileDocumentVO(fileDocument));
            } else if ("8".equals(opt)) {
                System.out.println("请输入音量调整百分比([0, 500]，如300即调大3倍)：");
                String input = inputFromConsole();
                Integer volume = Integer.parseInt(input);
                if (volume < 0 || volume > 500) return;
                fileDocument.setVolume(volume);
                fileDocumentController.update(new FileDocumentVO(fileDocument));
            } else if ("9".equals(opt)) {

            }
        }
    }

    private void gen() throws Exception {
        if (list.size() <= 0) {
            System.out.println("文件列表为空，请上传文件");
            return;
        }
        System.out.println("输出文件夹路径：");
        String directoryPath = inputFromConsole();
        System.out.println("分辨率宽：");
        Integer canvasWidth = Integer.parseInt(inputFromConsole());
        System.out.println("分辨率高");
        Integer canvasHeight = Integer.parseInt(inputFromConsole());
        File folder = new File(directoryPath);
        if (!folder.exists() && !folder.isDirectory()) {
            folder.mkdirs();
        }
        String tempPath = directoryPath + "/temp";
        File tempFoler = new File(tempPath);
        if (!tempFoler.exists() && !tempFoler.isDirectory()) {
            tempFoler.mkdirs();
        }
        System.out.println("正在初始化中...");
        String bgPath = tempPath + "/" + "bg.jpg";
        FFmpegUtil.pureColorPictures(canvasWidth, canvasHeight, bgPath);

        List<String> commands = new java.util.ArrayList<>();
        commands.add("ffmpeg");
        commands.add("-y");
        for(FileDocument fileDocument : list) {
            Optional<FileDocument> fileContent = documentService.getFileContent(fileDocument.getId());
            //TODO 各种视频格式的兼容
            String tempFilePath = tempPath+"/"+fileDocument.getId()+"."+fileDocument.getSuffix();
            saveFile(tempFilePath, fileContent.get().getContent());
            fileDocument.setFilePath(tempFilePath);
            FFmpegUtil.genInputCommands(commands, fileDocument);
        }
        commands.add("-i");
        commands.add(bgPath);
        commands.add("-filter_complex");
        StringBuilder builder = new StringBuilder();
        //有新结果才自增
        int preRes = -1;//上一次操作结果
        int preResAudio = -1;
        //视频流filter拼接
        for(int i = 0; i < list.size(); i++) {
            FileDocument file = list.get(i);
            //定义分辨率
            builder.append(String.format("[%d:v]scale=%d:%d[v%d];", i, file.getWidth(), file.getHeight(), ++preRes));
            if (file.getXCoordinate() == null) file.setXCoordinate(0);
            if (file.getYCoordinate() == null) file.setYCoordinate(0);
            if (i == 0) {
                //第一个视频流覆盖在背景图
                builder.append(String.format("[%d:v][v%d]overlay=%d:%d[v%d];", list.size(), preRes, file.getXCoordinate(), file.getYCoordinate(), ++preRes));
            } else {
                //其余视频覆盖在上次视频流操作结果
                builder.append(String.format("[v%d][v%d]overlay=%d:%d[v%d];", preRes-1, preRes, file.getXCoordinate(), file.getYCoordinate(), ++preRes));
            }
            //拼接音量
            if (file.getVolume() == null) file.setVolume(100);
            builder.append(String.format("[%d:a]volume=%s[a%d];", i, ToolUtil.rvZeroAndDot(String.valueOf(file.getVolume()/100f)), ++preResAudio));
            //拼接音频流
            if (i != 0) {
                builder.append(String.format("[a%d][a%d]amix=inputs=2:duration=longest[a%d];", preResAudio-1, preResAudio, ++preResAudio));
            }
        }
        builder.deleteCharAt(builder.length() - 1);
        commands.add(builder.toString());
        commands.add("-map");
        commands.add(String.format("[v%d]", preRes));
        commands.add("-map");
        commands.add(String.format("[a%d]", preResAudio));
        commands.add("-s");
        commands.add(canvasWidth+"x"+canvasHeight);
        String outputFilePath = directoryPath + "/" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + ".mp4";
        commands.add(outputFilePath);
        System.out.println("正在生成中...");
        System.out.println("生成的ffmpeg指令为："+ToolUtil.toJson(commands));
        try {
            ProcessBuilder pb = new ProcessBuilder();
            Process process = pb.command(commands).redirectErrorStream(true).start();
            //从输入流中读取视频信息
            BufferedReader br = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            StringBuffer sb = new StringBuffer();
            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            process.waitFor();
            process.destroy();
            System.out.println("导出成功，生成文件为：");
            System.out.println(outputFilePath);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("生成失败");
        }
    }


    public static void saveFile(String filepath,byte [] data)throws Exception{
        if(data != null){
            File file  = new File(filepath);
            if(file.exists()){
                file.delete();
            }
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(data,0,data.length);
            fos.flush();
            fos.close();
        }
    }

    private void printMetadata() {
        System.out.println("请选择查看的序号：");
        String number = inputFromConsole();
        int index = checkNum(number);
        if (index > -1) {
            FileDocument fileDocument = list.get(index);
            System.out.println("信息：");
            System.out.println("文件名：" + fileDocument.getName());
            System.out.println("时长：" + ToolUtil.longToTime(fileDocument.getDuration()));
            System.out.println("大小：" + ToolUtil.formatSize(fileDocument.getSize()));
            System.out.println("上传日期：" + new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(fileDocument.getUploadDate()));
            System.out.println("开始时间："+ToolUtil.longToTime(fileDocument.getStartTime()));
            System.out.println("结束时间："+ToolUtil.longToTime(fileDocument.getEndTime()));
            System.out.println("x坐标平移量："+(fileDocument.getXCoordinate() == null ? 0 : fileDocument.getYCoordinate()));
            System.out.println("y坐标平移量："+(fileDocument.getYCoordinate() == null ? 0 : fileDocument.getYCoordinate()));
            System.out.println("分辨率："+fileDocument.getWidth() + "x" +fileDocument.getHeight());
            System.out.println("偏移量："+(fileDocument.getOffset()==null?0:fileDocument.getOffset())+"s");
            System.out.println("音量："+fileDocument.getVolume()+"%");
        }
    }
}
