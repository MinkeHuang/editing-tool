**媒体剪辑工具Demo**

**当前支持的功能**：
1.轨道平移
2.画面x轴移动
3.画面y轴移动
4.画面缩放
5.开始时间设置
6.结束时间设置
7.层级设置
8.音量设置

**待开发的功能**
1.文件大小限制
2.画面裁剪
3.各种媒体格式的兼容及测试

**使用步骤**
1.src/main/resources/application.properties，文件配置本地Mongodb数据源
2.src/test/java/com/jeffrey/editingtool/EditingToolTest.java，找到测试类
3.运行Test方法com.jeffrey.editingtool.EditingToolTest.contextLoads，运行此测试方法
4.在控制台会显示菜单，按提示操作即可
