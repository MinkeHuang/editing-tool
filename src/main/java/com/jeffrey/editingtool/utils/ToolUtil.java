package com.jeffrey.editingtool.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jeffrey.editingtool.Constants.AbstractConstants;
import org.springframework.util.StringUtils;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author jeffreydou
 */
public class ToolUtil {
    public static String genUUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public static String getContentTypeBySuffix(String suffix) {
        if (Arrays.asList("jpg", "jpeg", "gif", "png", "bmp", "ico").contains(suffix))
            return AbstractConstants.TYPE_PIC;
        if (Arrays.asList("zip", "7z", "rar").contains(suffix))
            return AbstractConstants.TYPE_ZIP;
        if (Arrays.asList("doc", "docx", "ppt", "pptx", "xls", "xlsx", "pps", "pdf").contains(suffix))
            return AbstractConstants.TYPE_DOCUMENT;
        if (Arrays.asList("wmv", "mp4", "flv", "avi", "rmvb", "mpg", "mkv", "mov").contains(suffix))
            return AbstractConstants.TYPE_VIDEO;
        if (Arrays.asList("cda", "wav", "mp3", "wma", "aac", "ra", "midi", "ogg").contains(suffix))
            return AbstractConstants.TYPE_AUDIO;
        return AbstractConstants.TYPE_FILE;
    }

    /**
     * 获取该输入流的MD5值
     */
    public static String getMD5(InputStream is) throws NoSuchAlgorithmException, IOException {
        StringBuffer md5 = new StringBuffer();
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] dataBytes = new byte[1024];

        int nread = 0;
        while ((nread = is.read(dataBytes)) != -1) {
            md.update(dataBytes, 0, nread);
        }
        ;
        byte[] mdbytes = md.digest();

        // convert the byte to hex format
        for (int i = 0; i < mdbytes.length; i++) {
            md5.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
        }
        return md5.toString();
    }

    public static String longToTime(Long chargeTime) {
        if (chargeTime == null) {
            return null;
        }
        String chargeTimechargeTime;
        //时
        Long hour = chargeTime / 60 / 60;
        //分
        Long minutes = chargeTime / 60 % 60;
        //秒
        Long remainingSeconds = chargeTime % 60;
        //判断时分秒是否小于10……
        if (hour < 10 && minutes < 10 && remainingSeconds < 10) {
            chargeTimechargeTime = "0" + hour + ":" + "0" + minutes + ":" + "0" + remainingSeconds;
        } else if (hour < 10 && minutes < 10) {
            chargeTimechargeTime = "0" + hour + ":" + "0" + minutes + ":" + remainingSeconds;
        } else if (hour < 10 && remainingSeconds < 10) {
            chargeTimechargeTime = "0" + hour + ":" + minutes + ":" + "0" + remainingSeconds;
        } else if (minutes < 10 && remainingSeconds < 10) {
            chargeTimechargeTime = hour + ":" + "0" + minutes + ":" + "0" + remainingSeconds;
        } else if (hour < 10) {
            chargeTimechargeTime = "0" + hour + ":" + minutes + ":" + remainingSeconds;
        } else if (minutes < 10) {
            chargeTimechargeTime = hour + ":" + "0" + minutes + ":" + remainingSeconds;
        } else if (remainingSeconds < 10) {
            chargeTimechargeTime = hour + ":" + minutes + ":" + "0" + remainingSeconds;
        } else {
            chargeTimechargeTime = hour + ":" + minutes + ":" + remainingSeconds;
        }
        return chargeTimechargeTime;
    }

    // 格式:"00:00:10.68" -> 秒
    public static int getTimelen(String timelen) {
        int min = 0;
        String strs[] = timelen.split(":");
        if (strs[0].compareTo("0") > 0) {
            // 秒
            min += Integer.valueOf(strs[0]) * 60 * 60;
        }
        if (strs[1].compareTo("0") > 0) {
            min += Integer.valueOf(strs[1]) * 60;
        }
        if (strs[2].compareTo("0") > 0) {
            min += Math.round(Float.valueOf(strs[2]));
        }
        return min;
    }

    /**
     * 创建文件
     *
     * @param fileName
     * @return
     */
    public static boolean createFile(File fileName) throws Exception {
        boolean flag = false;
        try {
            if (!fileName.exists()) {
                fileName.createNewFile();
                flag = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    public static String rvZeroAndDot(String val) {
        if (val.indexOf(".") > 0) {
            val = val.replaceAll("0+?$", "");
            val = val.replaceAll("[.]$", "");
        }
        return val;
    }

    /**
     * 将object对象转成json格式字符串
     */
    public static String toJson(Object object) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setPrettyPrinting();
        Gson gson = gsonBuilder.create();
        return gson.toJson(object);
    }

    /**
     * 获取元数据
     * @param filePath
     * @return metadata元数据、duration时长
     */
    public static Map<String, Object> getMetadata(String filePath) {
        List<String> commands = new java.util.ArrayList<String>();
        commands.add("ffmpeg");
        commands.add("-i");
        commands.add(filePath);
        commands.add("-hide_banner");
        Map<String, Object> map = new HashMap<>();
        try {
            ProcessBuilder builder = new ProcessBuilder();
            builder.command(commands);
            final Process p = builder.start();

            //从输入流中读取视频信息
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            StringBuffer sb = new StringBuffer();
            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            br.close();
            map.put("metadata", sb.toString());
            //从视频信息中解析时长
            String regexDuration = "Duration: (.*?), start: (.*?), bitrate: (\\d* kb/s)";
            Pattern pattern = Pattern.compile(regexDuration);
            Matcher m = pattern.matcher(sb.toString());
            if (m.find()) {
                //TODO 保存毫秒
                map.put("duration", m.group(1));
                map.put("bitrate", m.group(3));
            }
            String size = ", [0-9]{0,}x[0-9]{0,}";
            pattern = Pattern.compile(size);
            m = pattern.matcher(sb.toString());
            if (m.find()) {
                map.put("resolution", m.group(0).substring(1));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return map;
    }

    public static long getVideoTime(String filePath) {
        Object timeObj = getMetadata(filePath).get("duration");
        if (timeObj == null) return 0;
        return getTimelen((String)timeObj);
    }


    public static String getType(long time) {
        return time < 10 ? "0" + time : String.valueOf(time);
    }

    public static String getTime(long duration) {
        long min = (duration) / 60;
        long sec = (duration) % 60;
        return getType(min) + ":" + getType(sec);

    }

    /**
     * 1kb=1024Byte
     * @param size
     * @return
     */
    public static String formatSize(Long size){
        if(size == null){
            return null;
        }
        if (size < 1024) {
            return String.valueOf(size) + "Byte";
        } else {
            size = size / 1024;
        }
        if (size < 1024) {
            //因为如果以Kb为单位的话，要保留最后1位小数，
            //因此，把此数乘以100之后再取余
            size = size * 100;
            return String.valueOf((size / 100)) + "."
                + String.valueOf((size % 100)) + "Kb";
        } else {
            //否则如果要以Mb为单位的，先除于1024再作同样的处理
            size = size * 100 / 1024;
            return String.valueOf((size / 100)) + "."
                + String.valueOf((size % 100)) + "Mb";
        }
    }

}
