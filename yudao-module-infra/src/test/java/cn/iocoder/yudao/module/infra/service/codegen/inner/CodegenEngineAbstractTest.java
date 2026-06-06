package cn.iocoder.yudao.module.infra.service.codegen.inner;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.ZipUtil;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.test.core.ut.BaseMockitoUnitTest;
import cn.iocoder.yudao.module.infra.dal.dataobject.codegen.CodegenColumnDO;
import cn.iocoder.yudao.module.infra.dal.dataobject.codegen.CodegenTableDO;
import cn.iocoder.yudao.module.infra.enums.codegen.CodegenVOTypeEnum;
import cn.iocoder.yudao.module.infra.framework.codegen.config.CodegenProperties;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Spy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link CodegenEngine} 的单元测试抽象基类
 *
 * @author 芋道源码
 */
public abstract class CodegenEngineAbstractTest extends BaseMockitoUnitTest {

    /**
     * 测试文件资源目录
     */
    private String resourcesPath = "";

    @InjectMocks
    protected CodegenEngine codegenEngine;

    @Spy
    protected CodegenProperties codegenProperties = new CodegenProperties()
            .setBasePackage("cn.iocoder.yudao")
            .setVoType(CodegenVOTypeEnum.VO.getType())
            .setDeleteBatchEnable(true)
            .setUnitTestEnable(true)
            .setImportEnable(false);

    @BeforeEach
    public void setUp() {
        codegenEngine.setJakartaEnable(true); // 强制使用 jakarta，保证单测可以基于 jakarta 断言
        codegenEngine.initGlobalBindingMap();
        // 获取测试文件 resources 路径，writeResult 调试用
        String absolutePath = FileUtil.getAbsolutePath("application-unit-test.yaml");
        resourcesPath = absolutePath.split("/target")[0] + "/src/test/resources/codegen/";
    }

    protected static CodegenTableDO getTable(String name) {
        String content = ResourceUtil.readUtf8Str("codegen/table/" + name + ".json");
        return JsonUtils.parseObject(content, "table", CodegenTableDO.class);
    }

    protected static List<CodegenColumnDO> getColumnList(String name) {
        String content = ResourceUtil.readUtf8Str("codegen/table/" + name + ".json");
        List<CodegenColumnDO> list = JsonUtils.parseArray(content, "columns", CodegenColumnDO.class);
        list.forEach(column -> {
            if (column.getNullable() == null) {
                column.setNullable(false);
            }
            if (column.getCreateOperation() == null) {
                column.setCreateOperation(false);
            }
            if (column.getUpdateOperation() == null) {
                column.setUpdateOperation(false);
            }
            if (column.getListOperation() == null) {
                column.setListOperation(false);
            }
            if (column.getListOperationResult() == null) {
                column.setListOperationResult(false);
            }
        });
        return list;
    }

    /**
     * 重新生成断言数据的开关，命令行加 {@code -Dcodegen.regenerate=true} 启用
     */
    private static final boolean REGENERATE = Boolean.parseBoolean(System.getProperty("codegen.regenerate", "false"));

    @SuppressWarnings("rawtypes")
    protected void assertResult(Map<String, String> result, String path) {
        if (REGENERATE) {
            writeResult(result, resourcesPath + path);
            return;
        }
        String assertContent = ResourceUtil.readUtf8Str("codegen/" + path + "/assert.json");
        List<HashMap> asserts = JsonUtils.parseArray(assertContent, HashMap.class);
        Set<String> expectedFiles = asserts.stream()
                .map(m -> (String) m.get("filePath"))
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
        assertEquals(expectedFiles, result.keySet(), "生成文件集合不匹配");
        // 校验每个文件；归一化 \r\n 为 \n，让断言不依赖文件落盘的换行风格
        asserts.forEach(assertMap -> {
            String contentPath = (String) assertMap.get("contentPath");
            String filePath = (String) assertMap.get("filePath");
            String expected = normalizeLineEndings(ResourceUtil.readUtf8Str("codegen/" + path + "/" + contentPath));
            String actual = result.get(filePath);
            assertEquals(expected, normalizeLineEndings(actual), filePath + "：不匹配");
        });
    }

    /**
     * 统一换行符并忽略文件末尾换行，避免 Windows/Unix 落盘差异导致快照断言失败
     */
    private static String normalizeLineEndings(String content) {
        if (content == null) {
            return null;
        }
        content = content.replace("\r\n", "\n").replace('\r', '\n');
        return content.replaceAll("\\n+\\z", "");
    }

    // ==================== 调试专用 ====================

    /**
     * 【调试使用】将生成的代码，写入到文件
     *
     * @param result 生成的代码
     * @param path   写入文件的路径
     */
    protected void writeFile(Map<String, String> result, String path) {
        // 生成压缩包
        String[] paths = result.keySet().toArray(new String[0]);
        ByteArrayInputStream[] ins = result.values().stream().map(IoUtil::toUtf8Stream).toArray(ByteArrayInputStream[]::new);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ZipUtil.zip(outputStream, paths, ins);
        // 写入文件
        FileUtil.writeBytes(outputStream.toByteArray(), path);
    }

    /**
     * 【调试使用】将生成的结果，写入到文件
     *
     * @param result   生成的代码
     * @param basePath 写入文件的路径（绝对路径）
     */
    protected void writeResult(Map<String, String> result, String basePath) {
        // 写入文件内容
        List<Map<String, String>> asserts = new ArrayList<>();
        result.forEach((filePath, fileContent) -> {
            String lastFilePath = StrUtil.subAfter(filePath, '/', true);
            String contentPath = StrUtil.subAfter(lastFilePath, '.', true)
                    + '/' + StrUtil.subBefore(lastFilePath, '.', true);
            asserts.add(MapUtil.<String, String>builder().put("filePath", filePath)
                    .put("contentPath", contentPath).build());
            FileUtil.writeUtf8String(fileContent, basePath + "/" + contentPath);
        });
        // 写入 assert.json 文件
        FileUtil.writeUtf8String(JsonUtils.toJsonPrettyString(asserts), basePath + "/assert.json");
    }

}
