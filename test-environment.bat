@echo off
echo 临时设置Java和Maven环境变量 (当前会话有效)

REM 设置当前会话的环境变量
set JAVA_HOME=C:\Users\x8440\.jdks\ms-17.0.17
set MAVEN_HOME=E:\apache-maven-3.9.12
set M2_HOME=E:\apache-maven-3.9.12
set PATH=%PATH%;%JAVA_HOME%\bin;%MAVEN_HOME%\bin

echo 当前会话环境变量已设置
echo JAVA_HOME: %JAVA_HOME%
echo MAVEN_HOME: %MAVEN_HOME%
echo.

echo 测试Java...
java -version

echo.
echo 测试Maven...
mvn --version

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ====================================
    echo Java和Maven配置成功！
    echo ====================================

    REM 测试编译项目
    echo.
    echo 测试编译后端项目...
    cd blog-backend
    mvn clean compile -q
    if %ERRORLEVEL% EQU 0 (
        echo 后端项目编译成功！
        echo.
        echo 现在可以启动后端项目：
        echo mvn spring-boot:run
    ) else (
        echo 后端项目编译失败
    )

) else (
    echo.
    echo ====================================
    echo Maven配置失败，请检查路径是否正确
    echo ====================================
)

pause