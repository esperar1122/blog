@echo off
echo 临时设置Maven环境变量 (当前会话有效)

REM 设置当前会话的环境变量
set MAVEN_HOME=E:\apache-maven-3.9.12
set M2_HOME=E:\apache-maven-3.9.12
set PATH=%PATH%;E:\apache-maven-3.9.12\bin

echo 当前会话Maven环境变量已设置
echo MAVEN_HOME: %MAVEN_HOME%
echo M2_HOME: %M2_HOME%

REM 测试Maven
echo.
echo 测试Maven安装...
mvn --version

if %ERRORLEVEL% EQU 0 (
    echo Maven配置成功！
) else (
    echo Maven配置失败，请检查路径是否正确
)

pause