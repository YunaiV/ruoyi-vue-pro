package cn.iocoder.dashboard.modules.infra.controller.doc;

import cn.hutool.core.lang.UUID;
import cn.iocoder.dashboard.util.servlet.ServletUtils;
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
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Collections;

@Api(tags = "数据库文档")
@RestController
@RequestMapping("/infra/db-doc")
public class InfDbDocController {

    @Resource
    private DataSourceProperties dataSourceProperties;

    private static final String FILE_OUTPUT_DIR = System.getProperty("java.io.tmpdir") + File.separator
            + "db-doc";
    private static final String DOC_FILE_NAME = "数据库文档";
    private static final String DOC_VERSION = "1.0.0";
    private static final String DOC_DESCRIPTION = "文档描述";


    @GetMapping("/export-html")
    public void exportHtml(@RequestParam(defaultValue = "true") Boolean deleteFile,
                                        HttpServletResponse response) throws IOException {
        EngineFileType fileOutputType=EngineFileType.HTML;
        doExportFile(fileOutputType,deleteFile,response);
    }



    @GetMapping("/export-word")
    public void exportWord(@RequestParam(defaultValue = "true") Boolean deleteFile,
                                        HttpServletResponse response) throws IOException {
        EngineFileType fileOutputType=EngineFileType.WORD;
        doExportFile(fileOutputType,deleteFile,response);
    }


    @GetMapping("/export-markdown")
    public void exportMarkdown(@RequestParam(defaultValue = "true") Boolean deleteFile,
            HttpServletResponse response) throws IOException {
        EngineFileType fileOutputType=EngineFileType.MD;
        doExportFile(fileOutputType,deleteFile,response);
    }

    private void doExportFile(EngineFileType fileOutputType, Boolean deleteFile,
                              HttpServletResponse response) throws IOException {
        String docFileName=DOC_FILE_NAME+"_"+ UUID.fastUUID().toString(true);
        String filePath= doExportFile(fileOutputType,docFileName);
        String downloadFileName=DOC_FILE_NAME+fileOutputType.getFileSuffix(); //下载后的文件名
        // 读取，返回
        try (InputStream is=new FileInputStream(filePath)){//处理后关闭文件流才能删除
            ServletUtils.writeAttachment(response,downloadFileName, StreamUtils.copyToByteArray(is));
        }
        handleDeleteFile(deleteFile,filePath);
    }


    /**
     * 输出文件，返回文件路径
     * @param fileOutputType
     * @param fileName
     * @return
     */
    private String doExportFile(EngineFileType fileOutputType, String fileName){
        try (HikariDataSource dataSource = buildDataSource()) {
            // 创建 screw 的配置
            Configuration config = Configuration.builder()
                    .version(DOC_VERSION)  // 版本
                    .description(DOC_DESCRIPTION) // 描述
                    .dataSource(dataSource) // 数据源
                    .engineConfig(buildEngineConfig(fileOutputType,fileName)) // 引擎配置
                    .produceConfig(buildProcessConfig()) // 处理配置
                    .build();

            // 执行 screw，生成数据库文档
            new DocumentationExecute(config).execute();


            String filePath=FILE_OUTPUT_DIR + File.separator + fileName + fileOutputType.getFileSuffix();
            return filePath;
        }
    }

    private void handleDeleteFile(Boolean deleteFile,String filePath){
        if(!deleteFile){
            return;
        }
        File file=new File(filePath);
        file.delete();
    }

    /**
     * 创建数据源
     */
    // TODO 芋艿：screw 暂时不支持 druid，尴尬
    private HikariDataSource buildDataSource() {
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
    private static EngineConfig buildEngineConfig(EngineFileType fileOutputType,String docFileName) {
        return EngineConfig.builder()
                .fileOutputDir(FILE_OUTPUT_DIR) // 生成文件路径
                .openOutputDir(false) // 打开目录
                .fileType(fileOutputType) // 文件类型
                .produceType(EngineTemplateType.freemarker) // 文件类型
                .fileName(docFileName) // 自定义文件名称
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
