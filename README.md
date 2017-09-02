# ted_download

##操作步骤
1. 从metated.petarmaric.com获取ted视频链接xml。

2. 解析xml，获取ted视频下载链接

3. 对比已下载的视频文件。列出未下载的视频链接。


下载文件使用小米路由远程下载，小米路由提供samba服务。

供代码参考

##required
JDK >= 1.8


##java
使用vertx3的core包，用于下载操作


##python/move_ted_by_year.py
将下载的文件按年代分类保存