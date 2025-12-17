@echo off
echo ========================================
echo  Java 17 安装脚本 for Blog Backend
echo ========================================
echo.

echo 检查当前Java版本...
java -version
echo.

echo 这个项目需要Java 17或更高版本。
echo 请选择安装方式:
echo.
echo 1. 使用Chocolatey安装 (推荐，需要先安装Chocolatey)
echo 2. 手动下载安装
echo 3. 临时跳过 (需要后续手动配置)
echo.
set /p choice="请选择 (1-3): "

if "%choice%"=="1" (
    echo.
    echo 使用Chocolatey安装Java 17...
    choco install temurin17 -y
    if %errorlevel%==0 (
        echo Java 17安装成功！
        echo 请重新打开命令行窗口后运行项目。
    ) else (
        echo 安装失败，请检查Chocolatey是否正确安装。
    )
)

if "%choice%"=="2" (
    echo.
    echo 正在打开Java 17下载页面...
    echo 请下载并安装 'Temurin JDK 17 LTS' for Windows x64
    echo.
    start https://adoptium.net/temurin/releases/?version=17&os=windows&arch=x64
    echo.
    echo 安装完成后，请设置环境变量:
    echo JAVA_HOME = [Java 17安装路径]
    echo PATH = %%JAVA_HOME%%\bin;%%PATH%%
    echo.
    pause
)

if "%choice%"=="3" (
    echo.
    echo 跳过Java安装。请注意：
    echo - 项目无法在当前Java 8环境下编译
    echo - 需要后续手动安装Java 17
    echo - 查看 ENVIRONMENT_SETUP.md 获取详细说明
    echo.
)

echo.
echo 安装完成后，请运行以下命令验证:
echo java -version
echo mvn -version
echo.
pause