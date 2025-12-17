@echo off
echo ====================================
echo 配置Maven环境变量
echo ====================================

REM 设置Maven环境变量
setx MAVEN_HOME "E:\apache-maven-3.9.12" /M
setx M2_HOME "E:\apache-maven-3.9.12" /M

REM 将Maven的bin目录添加到PATH
setx PATH "%PATH%;E:\apache-maven-3.9.12\bin" /M

echo ====================================
echo Maven环境变量配置完成！
echo ====================================
echo.
echo MAVEN_HOME: E:\apache-maven-3.9.12
echo M2_HOME: E:\apache-maven-3.9.12
echo PATH: 已添加Maven bin目录
echo.
echo 请重新打开命令行窗口以使环境变量生效！
echo.
pause