@echo off
echo 正在安装本地JAR文件到Maven本地仓库...
call mvn validate

echo 正在编译项目...
call mvn compile

echo 正在运行应用程序...
call mvn exec:java

pause 