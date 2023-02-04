package cn.iocoder.yudao.module.infra.controller.admin.db;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.iocoder.yudao.framework.common.util.servlet.ServletUtils;
import cn.smallbun.screw.core.Configuration;
import cn.smallbun.screw.core.engine.EngineConfig;
import cn.smallbun.screw.core.engine.EngineFileType;
import cn.smallbun.screw.core.engine.EngineTemplateType;
import cn.smallbun.screw.core.execute.DocumentationExecute;
import cn.smallbun.screw.core.process.ProcessConfig;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DataSourceProperty;
import com.baomidou.dynamic.datasource.spring.boot.autoconfigure.DynamicDataSourceProperties;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

@Tag(name = "管理后台 - 数据库文档")
@RestController
@RequestMapping("/infra/db-doc")
public class DatabaseDocController {

    @Resource
    private DynamicDataSourceProperties dynamicDataSourceProperties;

    private static final String FILE_OUTPUT_DIR = System.getProperty("java.io.tmpdir") + File.separator
            + "db-doc";
    private static final String DOC_FILE_NAME = "数据库文档";
    private static final String DOC_VERSION = "1.0.0";
    private static final String DOC_DESCRIPTION = "文档描述";

    @GetMapping("/export-html")
    @Operation(summary = "导出 html 格式的数据文档")
    @Parameter(name = "deleteFile", description = "是否删除在服务器本地生成的数据库文档", example = "true")
    public void exportHtml(@RequestParam(defaultValue = "true") Boolean deleteFile,
                           HttpServletResponse response) throws IOException {
        doExportFile(EngineFileType.HTML, deleteFile, response);
    }

    @GetMapping("/export-word")
    @Operation(summary = "导出 word 格式的数据文档")
    @Parameter(name = "deleteFile", description = "是否删除在服务器本地生成的数据库文档", example = "true")
    public void exportWord(@RequestParam(defaultValue = "true") Boolean deleteFile,
                           HttpServletResponse response) throws IOException {
        doExportFile(EngineFileType.WORD, deleteFile, response);
    }

    @GetMapping("/export-markdown")
    @Operation(summary = "导出 markdown 格式的数据文档")
    @Parameter(name = "deleteFile", description = "是否删除在服务器本地生成的数据库文档", example = "true")
    public void exportMarkdown(@RequestParam(defaultValue = "true") Boolean deleteFile,
                               HttpServletResponse response) throws IOException {
        doExportFile(EngineFileType.MD, deleteFile, response);
    }

    private void doExportFile(EngineFileType fileOutputType, Boolean deleteFile,
                              HttpServletResponse response) throws IOException {
        String docFileName = DOC_FILE_NAME + "_" + IdUtil.fastSimpleUUID();
        String filePath = doExportFile(fileOutputType, docFileName);
        String downloadFileName = DOC_FILE_NAME + fileOutputType.getFileSuffix(); //下载后的文件名
        try {
            // 读取，返回
            ServletUtils.writeAttachment(response, downloadFileName, FileUtil.readBytes(filePath));
        } finally {
            handleDeleteFile(deleteFile, filePath);
        }
    }

    /**
     * 输出文件，返回文件路径
     *
     * @param fileOutputType 文件类型
     * @param fileName       文件名, 无需 ".docx" 等文件后缀
     * @return 生成的文件所在路径
     */
    private String doExportFile(EngineFileType fileOutputType, String fileName) {
        try (HikariDataSource dataSource = buildDataSource()) {
            // 创建 screw 的配置
            Configuration config = Configuration.builder()
                    .version(DOC_VERSION)  // 版本
                    .description(DOC_DESCRIPTION) // 描述
                    .dataSource(dataSource) // 数据源
                    .engineConfig(buildEngineConfig(fileOutputType, fileName)) // 引擎配置
                    .produceConfig(buildProcessConfig()) // 处理配置
                    .build();

            // 执行 screw，生成数据库文档
            new DocumentationExecute(config).execute();

            return FILE_OUTPUT_DIR + File.separator + fileName + fileOutputType.getFileSuffix();
        }
    }

    private void handleDeleteFile(Boolean deleteFile, String filePath) {
        if (!deleteFile) {
            return;
        }
        FileUtil.del(filePath);
    }

    /**
     * 创建数据源
     */
    // TODO 芋艿：screw 暂时不支持 druid，尴尬
    private HikariDataSource buildDataSource() {
        // 获得 DataSource 数据源，目前只支持首个
        String primary = dynamicDataSourceProperties.getPrimary();
        DataSourceProperty dataSourceProperty = dynamicDataSourceProperties.getDatasource().get(primary);
        // 创建 HikariConfig 配置类
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(dataSourceProperty.getUrl());
        hikariConfig.setUsername(dataSourceProperty.getUsername());
        hikariConfig.setPassword(dataSourceProperty.getPassword());
        hikariConfig.addDataSourceProperty("useInformationSchema", "true"); // 设置可以获取 tables remarks 信息
        // 创建数据源
        return new HikariDataSource(hikariConfig);
    }

    /**
     * 创建 screw 的引擎配置
     */
    private static EngineConfig buildEngineConfig(EngineFileType fileOutputType, String docFileName) {
        return EngineConfig.builder()
                .fileOutputDir(FILE_OUTPUT_DIR) // 生成文件路径
                .openOutputDir(false) // 打开目录
                .fileType(fileOutputType) // 文件类型
                .produceType(EngineTemplateType.velocity) // 文件类型
                .fileName(docFileName) // 自定义文件名称
                .build();
    }

    /**
     * 创建 screw 的处理配置，一般可忽略
     * 指定生成逻辑、当存在指定表、指定表前缀、指定表后缀时，将生成指定表，其余表不生成、并跳过忽略表配置
     */
    private static ProcessConfig buildProcessConfig() {
        return ProcessConfig.builder()
                .ignoreTablePrefix(Arrays.asList("QRTZ_", "ACT_")) // 忽略表前缀
                .build();
    }

}
