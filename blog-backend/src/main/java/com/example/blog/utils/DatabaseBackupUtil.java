package com.example.blog.utils;

import com.example.blog.entity.BackupRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class DatabaseBackupUtil {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Value("${app.backup.path:/tmp/backups}")
    private String backupPath;

    @Value("${spring.datasource.url}")
    private String databaseUrl;

    @Value("${spring.datasource.username}")
    private String databaseUsername;

    @Value("${spring.datasource.password}")
    private String databasePassword;

    @Value("${app.backup.mysql.path:mysqldump}")
    private String mysqldumpPath;

    @Value("${app.backup.mysql.path:mysql}")
    private String mysqlPath;

    /**
     * 创建数据库备份
     */
    public String createDatabaseBackup(BackupRecord backupRecord) throws IOException, InterruptedException {
        log.info("开始创建数据库备份: {}", backupRecord.getName());

        // 确保备份目录存在
        Path backupDir = Paths.get(backupPath);
        if (!Files.exists(backupDir)) {
            Files.createDirectories(backupDir);
        }

        // 生成备份文件名
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String fileName = String.format("%s_%s.sql", backupRecord.getName(), timestamp);
        if (backupRecord.getCompressed()) {
            fileName += ".gz";
        }

        Path backupFile = backupDir.resolve(fileName);

        // 构建mysqldump命令
        List<String> command = buildMysqldumpCommand(backupRecord, backupFile.toString());

        log.info("执行备份命令: {}", String.join(" ", command));

        // 执行备份命令
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectErrorStream(true);

        Process process = processBuilder.start();

        // 读取输出和错误信息
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }

        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new RuntimeException("数据库备份失败，退出码: " + exitCode + ", 输出: " + output);
        }

        log.info("数据库备份完成: {}", backupFile.toString());
        return backupFile.toString();
    }

    /**
     * 恢复数据库
     */
    public void restoreDatabase(String backupFilePath) throws IOException, InterruptedException {
        log.info("开始恢复数据库: {}", backupFilePath);

        // 检查备份文件是否存在
        File backupFile = new File(backupFilePath);
        if (!backupFile.exists()) {
            throw new RuntimeException("备份文件不存在: " + backupFilePath);
        }

        // 构建mysql命令
        List<String> command = buildMysqlCommand(backupFilePath);

        log.info("执行恢复命令: {}", String.join(" ", command));

        // 执行恢复命令
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectErrorStream(true);

        Process process = processBuilder.start();

        // 读取输出和错误信息
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }

        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new RuntimeException("数据库恢复失败，退出码: " + exitCode + ", 输出: " + output);
        }

        log.info("数据库恢复完成: {}", backupFilePath);
    }

    /**
     * 生成文件校验和
     */
    public String generateChecksum(String filePath) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (InputStream fis = new FileInputStream(filePath)) {
            byte[] byteArray = new byte[1024];
            int bytesCount;

            while ((bytesCount = fis.read(byteArray)) != -1) {
                digest.update(byteArray, 0, bytesCount);
            }
        }

        byte[] bytes = digest.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }

        return sb.toString();
    }

    /**
     * 构建mysqldump命令
     */
    private List<String> buildMysqldumpCommand(BackupRecord backupRecord, String outputPath) {
        List<String> command = new ArrayList<>();

        // mysqldump命令路径
        command.add(mysqldumpPath);

        // 连接参数
        extractDatabaseInfo();
        command.add("-h" + extractDatabaseHost());
        command.add("-P" + extractDatabasePort());
        command.add("-u" + databaseUsername);
        command.add("-p" + databasePassword);

        // 选项
        command.add("--single-transaction");
        command.add("--routines");
        command.add("--triggers");

        if (backupRecord.getCompressed()) {
            command.add("--compress");
        }

        // 根据配置决定是否包含结构
        if (!backupRecord.getIncludeStructure()) {
            command.add("--no-create-info");
        }

        // 根据配置决定是否包含数据
        if (!backupRecord.getIncludeData()) {
            command.add("--no-data");
        }

        // 根据配置决定是否包含触发器
        if (!backupRecord.getIncludeTriggers()) {
            command.add("--skip-triggers");
        }

        // 根据配置决定是否包含存储过程和函数
        if (!backupRecord.getIncludeRoutines()) {
            command.add("--skip-routines");
        }

        // 数据库名
        String databaseName = extractDatabaseName();
        command.add(databaseName);

        // 表过滤
        if (backupRecord.getIncludeTables() != null && !backupRecord.getIncludeTables().trim().isEmpty()) {
            String[] tables = backupRecord.getIncludeTables().split(",");
            for (String table : tables) {
                command.add(table.trim());
            }
        } else if (backupRecord.getExcludeTables() != null && !backupRecord.getExcludeTables().trim().isEmpty()) {
            // 忽略指定的表
            command.add("--ignore-table=" + databaseName + "." + backupRecord.getExcludeTables().replace(",", " --ignore-table=" + databaseName + "."));
        }

        // 输出重定向
        if (backupRecord.getCompressed()) {
            command.add("|");
            command.add("gzip");
            command.add(">");
            command.add(outputPath);
        } else {
            command.add(">");
            command.add(outputPath);
        }

        return command;
    }

    /**
     * 构建mysql恢复命令
     */
    private List<String> buildMysqlCommand(String backupFilePath) {
        List<String> command = new ArrayList<>();

        // 如果是压缩文件，需要先解压
        if (backupFilePath.endsWith(".gz")) {
            command.add("gunzip");
            command.add("-c");
            command.add(backupFilePath);
            command.add("|");
        }

        command.add(mysqlPath);

        // 连接参数
        extractDatabaseInfo();
        command.add("-h" + extractDatabaseHost());
        command.add("-P" + extractDatabasePort());
        command.add("-u" + databaseUsername);
        command.add("-p" + databasePassword);

        // 数据库名
        command.add(extractDatabaseName());

        return command;
    }

    /**
     * 提取数据库名
     */
    private String extractDatabaseName() {
        // 从URL中提取数据库名
        String url = databaseUrl;
        int lastSlash = url.lastIndexOf('/');
        int questionMark = url.indexOf('?');

        if (lastSlash != -1 && questionMark != -1) {
            return url.substring(lastSlash + 1, questionMark);
        } else if (lastSlash != -1) {
            return url.substring(lastSlash + 1);
        }

        return "blog_db"; // 默认数据库名
    }

    /**
     * 提取数据库主机
     */
    private String extractDatabaseHost() {
        String url = databaseUrl;
        if (url.contains("localhost")) {
            return "localhost";
        } else if (url.contains("127.0.0.1")) {
            return "127.0.0.1";
        }
        return "localhost"; // 默认主机
    }

    /**
     * 提取数据库端口
     */
    private String extractDatabasePort() {
        String url = databaseUrl;
        int portIndex = url.indexOf(":3306");
        if (portIndex != -1) {
            return "3306";
        }
        return "3306"; // 默认端口
    }

    /**
     * 提取数据库连接信息
     */
    private void extractDatabaseInfo() {
        // 确保数据库连接信息已设置
        if (databaseUsername == null) {
            try {
                databaseUsername = jdbcTemplate.queryForObject("SELECT USER()", String.class);
            } catch (Exception e) {
                log.warn("无法获取数据库用户名", e);
                databaseUsername = "root";
            }
        }
    }
}