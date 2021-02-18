package cn.iocoder.dashboard.modules.infra.controller.doc;

import cn.hutool.extra.servlet.ServletUtil;
import cn.smallbun.screw.core.Configuration;
import cn.smallbun.screw.core.engine.EngineConfig;
import cn.smallbun.screw.core.engine.EngineFileType;
import cn.smallbun.screw.core.engine.EngineTemplateType;
import cn.smallbun.screw.core.execute.DocumentationExecute;
import cn.smallbun.screw.core.process.ProcessConfig;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.swagger.annotations.Api;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Collections;

@Api(tags = "数据库文档")
@RestController
@RequestMapping("/infra/db-doc")
public class InfDbDocController {

    @Resource
    private DataSourceProperties dataSourceProperties;

    private static final String FILE_OUTPUT_DIR = System.getProperty("java.io.tmpdir") + File.separator
            + "db-doc";
    private static final EngineFileType FILE_OUTPUT_TYPE = EngineFileType.HTML; // 可以设置 Word 或者 Markdown 格式
    private static final String DOC_FILE_NAME = "数据库文档";
    private static final String DOC_VERSION = "1.0.0";
    private static final String DOC_DESCRIPTION = "文档描述";

    @Resource
    private DataSource dataSource;

    @GetMapping("/export-html")
    public synchronized void exportHtml(HttpServletResponse response) throws FileNotFoundException {
        // 创建 screw 的配置
        Configuration config = Configuration.builder()
                .version(DOC_VERSION)  // 版本
                .description(DOC_DESCRIPTION) // 描述
                .dataSource(buildDataSource()) // 数据源
                .engineConfig(buildEngineConfig()) // 引擎配置
                .produceConfig(buildProcessConfig()) // 处理配置
                .build();

        // 执行 screw，生成数据库文档
        new DocumentationExecute(config).execute();

        // 读取，返回
        ServletUtil.write(response,
                new FileInputStream(FILE_OUTPUT_DIR + File.separator + DOC_FILE_NAME + FILE_OUTPUT_TYPE.getFileSuffix()),
                MediaType.TEXT_HTML_VALUE);
    }

    /**
     * 创建数据源
     */
    private DataSource buildDataSource() {
        // 创建 HikariConfig 配置类
        HikariConfig hikariConfig = new HikariConfig();
//        hikariConfig.setDriverClassName("com.mysql.cj.jdbc.Driver");
        hikariConfig.setJdbcUrl(dataSourceProperties.getUrl());
        hikariConfig.setUsername(dataSourceProperties.getUsername());
        hikariConfig.setPassword(dataSourceProperties.getPassword());
        hikariConfig.addDataSourceProperty("useInformationSchema", "true"); // 设置可以获取 tables remarks 信息
        // 创建数据源
        return new HikariDataSource(hikariConfig);
    }

    /**
     * 创建 screw 的引擎配置
     */
    private static EngineConfig buildEngineConfig() {
        return EngineConfig.builder()
                .fileOutputDir(FILE_OUTPUT_DIR) // 生成文件路径
                .openOutputDir(false) // 打开目录
                .fileType(FILE_OUTPUT_TYPE) // 文件类型
                .produceType(EngineTemplateType.freemarker) // 文件类型
                .fileName(DOC_FILE_NAME) // 自定义文件名称
                .build();
    }

    /**
     * 创建 screw 的处理配置，一般可忽略
     * 指定生成逻辑、当存在指定表、指定表前缀、指定表后缀时，将生成指定表，其余表不生成、并跳过忽略表配置
     */
    private static ProcessConfig buildProcessConfig() {
        return ProcessConfig.builder()
                .ignoreTablePrefix(Collections.singletonList("QRTZ_")) // 忽略表前缀
                .build();
    }

}
