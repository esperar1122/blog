@echo off
echo ========================================
echo  Blog Backend 启动脚本
echo ========================================
echo.

echo 检查Java版本...
java -version
if %errorlevel% neq 0 (
    echo 错误: 未找到Java。请先安装Java 17。
    echo 运行 install_java17.bat 进行安装。
    pause
    exit /b 1
)

echo.
echo 检查Maven...
mvn -version
if %errorlevel% neq 0 (
    echo 错误: 未找到Maven。请先安装Maven。
    pause
    exit /b 1
)

echo.
echo 检查数据库连接...
mysql -u root -p123456 -e "USE blog_db;" 2>nul
if %errorlevel% neq 0 (
    echo 警告: 无法连接到数据库。
    echo 请确保MySQL服务正在运行，且blog_db数据库已创建。
    echo.
    echo 数据库配置:
    echo - 主机: localhost:3306
    echo - 数据库: blog_db
    echo - 用户名: root
    echo - 密码: 123456
    echo.
    set /p continue="是否继续启动应用? (y/n): "
    if /i not "%continue%"=="y" (
        pause
        exit /b 1
    )
)

echo.
echo 正在编译项目...
mvn clean compile
if %errorlevel% neq 0 (
    echo 错误: 编译失败。请检查代码和依赖。
    pause
    exit /b 1
)

echo.
echo 编译成功！正在启动应用...
echo 应用将在 http://localhost:8080/api 启动
echo 健康检查端点: http://localhost:8080/api/health
echo.
echo 按 Ctrl+C 停止应用
echo.

mvn spring-boot:run