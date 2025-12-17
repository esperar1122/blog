@echo off
echo ====================================
echo 配置Java和Maven环境变量
echo ====================================

REM Java路径 (根据实际安装情况调整)
set JAVA_HOME=C:\Users\x8440\.jdks\ms-17.0.17
set MAVEN_HOME=E:\apache-maven-3.9.12
set M2_HOME=E:\apache-maven-3.9.12

REM 设置永久环境变量
setx JAVA_HOME "%JAVA_HOME%" /M
setx MAVEN_HOME "%MAVEN_HOME%" /M
setx M2_HOME "%M2_HOME%" /M
setx PATH "%PATH%;%JAVA_HOME%\bin;%MAVEN_HOME%\bin" /M

echo ====================================
echo 环境变量配置完成！
echo ====================================
echo.
echo JAVA_HOME: %JAVA_HOME%
echo MAVEN_HOME: %MAVEN_HOME%
echo M2_HOME: %M2_HOME%
echo PATH: 已添加Java和Maven bin目录
echo.
echo 请重新打开命令行窗口以使环境变量生效！
echo.
pause