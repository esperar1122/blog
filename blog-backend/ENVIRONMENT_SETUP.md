# 后端环境配置指南

## 系统要求

### Java版本要求
- **必需版本**: Java 17 或更高版本
- **当前系统版本**: Java 1.8.0_181 (不兼容)

### 为什么需要Java 17？
- Spring Boot 3.2.0 最低要求Java 17
- 项目使用了Java 17+的新特性

## 解决方案

### 方案1: 安装Java 17 (推荐)

#### Windows安装步骤：
1. 下载Java 17 LTS:
   - 访问: https://adoptium.net/temurin/releases/?version=17
   - 下载 Windows x64 版本的 JDK

2. 安装Java 17:
   - 运行下载的安装程序
   - 记录安装路径 (例如: `C:\Program Files\Eclipse Adoptium\jdk-17.x.x.x`)

3. 配置环境变量:
   ```cmd
   # 设置JAVA_HOME
   setx JAVA_HOME "C:\Program Files\Eclipse Adoptium\jdk-17.x.x.x" /M

   # 更新PATH
   setx PATH "%JAVA_HOME%\bin;%PATH%" /M
   ```

4. 验证安装:
   ```cmd
   java -version
   # 应该显示: java version "17.x.x.x"
   ```

#### 使用包管理器安装:
**使用Chocolatey:**
```cmd
choco install temurin17
```

**使用Scoop:**
```cmd
scoop install openjdk17
```

### 方案2: 降级Spring Boot版本 (临时方案)

如果暂时无法安装Java 17，可以降级到Spring Boot 2.7.x:

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>2.7.18</version>
    <relativePath/>
</parent>

<properties>
    <java.version>8</java.version>
    <mybatis-plus.version>3.5.4.1</mybatis-plus.version>
</properties>
```

## 数据库配置

### MySQL配置
- 确保MySQL服务正在运行
- 数据库已创建: `blog_db`
- 连接信息:
  - 主机: localhost:3306
  - 数据库: blog_db
  - 用户名: root
  - 密码: 123456

### 创建数据库脚本
```sql
CREATE DATABASE IF NOT EXISTS blog_db
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;
```

## 项目配置

### 环境变量文件 (.env)
项目已创建 `.env` 文件，包含以下配置:
```
DB_PASSWORD=123456
JWT_SECRET=mySecretKey123456789mySecretKey123456789
SERVER_PORT=8080
```

### 应用配置
- 主配置文件: `application.yml`
- 开发环境配置: `application-dev.yml`
- 默认激活配置: `dev`

## 启动步骤

1. **确保Java 17已安装**
2. **配置数据库**
3. **安装依赖**:
   ```cmd
   cd blog-backend
   mvn clean install
   ```

4. **启动应用**:
   ```cmd
   mvn spring-boot:run
   ```

   或者在IDE中运行 `BlogApplication.java`

5. **验证启动**:
   - 访问: http://localhost:8080/api/health
   - 应该返回健康检查信息

## 常见问题

### Q: Maven编译失败
A: 检查Java版本是否正确设置
```cmd
echo %JAVA_HOME%
java -version
mvn -version
```

### Q: 数据库连接失败
A: 检查MySQL服务是否启动，密码是否正确

### Q: 端口被占用
A: 修改 `application-dev.yml` 中的端口号

## 开发工具推荐

- IDE: IntelliJ IDEA 或 Eclipse
- 数据库工具: MySQL Workbench, DBeaver
- API测试: Postman 或 Insomnia

## 下一步

环境配置完成后，可以:
1. 运行数据库迁移脚本
2. 启动应用
3. 测试API端点
4. 开始开发新功能