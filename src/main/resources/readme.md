# idea intellij 连接hadoopHDFS插件
源码编译：https://github.com/Donaldhan/HadoopIntellijPlugin
直接下载：https://blog.csdn.net/kismet2399/article/details/85090839

# IDEA远程调试mapreduce程序

配置
1. 首先从拷贝集群的hadoop的文件夹，放在电脑磁盘的一个目录，这里我放在D盘

2. 在本地配置环境变量：
HADOOP_HOME=F:\Hadoop\hadoop-2.7.1
HADOOP_BIN_PATH=%HADOOP_HOME%\bin
HADOOP_PREFIX=F:\Hadoop\hadoop-2.7.1

3. 如果要在window上运行，必须需要对应版本的hadoop.dll放到C;\Windows\System32下
还有对应版本的winutils.exe放在F:\Hadoop\hadoop-2.7.1
下载：
https://bbs.csdn.net/topics/392355483

https://blog.csdn.net/cy4ttty/article/details/84321503

4. 打包，上传，是否有插件

更多参考：

https://blog.csdn.net/qq_35347459/article/details/75948358

如果出现一下错误：

 1. Could not locate executable null\bin\winutils.exe in the Hadoop binaries.

 主要是由于HADOOP_HOME环境变量不存在，或者的winutils.exe，注意hadoop.dll， winutils.exe
 版本应该与hadoop的版本对应。


 2. administrator没有权限访问集群上hadoop中的/tmp目录
 这是由于本机的administrator没有操作hadoop目录的权限，需要配置hadoop_name，为方便起见就不在环境变量里面配置，直接在程序中加入：

 System.setProperty("HADOOP_USER_NAME", "donaldhan");
