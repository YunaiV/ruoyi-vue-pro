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
import cn.iocoder.yudao.module.infra.enums.codegen.*;
import cn.iocoder.yudao.module.infra.framework.codegen.config.CodegenProperties;
import org.apache.ibatis.type.JdbcType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Spy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link CodegenEngine} 的单元测试
 *
 * @author 芋道源码
 */
public class CodegenEngineTest extends BaseMockitoUnitTest {

    @InjectMocks
    private CodegenEngine codegenEngine;

    @Spy
    private CodegenProperties codegenProperties = new CodegenProperties()
            .setBasePackage("cn.iocoder.yudao");

    @BeforeEach
    public void setUp() {
        codegenEngine.initGlobalBindingMap();
    }

    @Test
    public void testExecute_vue3_one() {
        // 准备参数
        CodegenTableDO table = getTable("student")
                .setFrontType(CodegenFrontTypeEnum.VUE3.getType())
                .setTemplateType(CodegenTemplateTypeEnum.ONE.getType());
        List<CodegenColumnDO> columns = getColumnList("student");

        // 调用
        Map<String, String> result = codegenEngine.execute(table, columns, null, null);
        // 断言
        assertResult(result, "codegen/vue3_one");
//        writeResult(result, "/root/ruoyi-vue-pro/yudao-module-infra/yudao-module-infra-biz/src/test/resources/codegen/vue3_one");
    }

    @Test
    public void testExecute_vue3_tree() {
        // 准备参数
        CodegenTableDO table = getTable("category")
                .setFrontType(CodegenFrontTypeEnum.VUE3.getType())
                .setTemplateType(CodegenTemplateTypeEnum.TREE.getType());
        List<CodegenColumnDO> columns = getColumnList("category");

        // 调用
        Map<String, String> result = codegenEngine.execute(table, columns, null, null);
        // 断言
        assertResult(result, "codegen/vue3_tree");
//        writeResult(result, "/root/ruoyi-vue-pro/yudao-module-infra/yudao-module-infra-biz/src/test/resources/codegen/vue3_tree");
//        writeFile(result, "/Users/yunai/test/demo66.zip");
    }

    @Test
    public void testExecute_vue3_master_normal() {
        testExecute_vue3_master(CodegenTemplateTypeEnum.MASTER_NORMAL, "codegen/vue3_master_normal");
    }

    @Test
    public void testExecute_vue3_master_erp() {
        testExecute_vue3_master(CodegenTemplateTypeEnum.MASTER_ERP, "codegen/vue3_master_erp");
    }

    @Test
    public void testExecute_vue3_master_inner() {
        testExecute_vue3_master(CodegenTemplateTypeEnum.MASTER_INNER, "codegen/vue3_master_inner");
    }

    private void testExecute_vue3_master(CodegenTemplateTypeEnum templateType,
                                         String path) {
        // 准备参数
        CodegenTableDO table = getTable("student")
                .setFrontType(CodegenFrontTypeEnum.VUE3.getType())
                .setTemplateType(templateType.getType());
        List<CodegenColumnDO> columns = getColumnList("student");
        // 准备参数（子表）
        CodegenTableDO contactTable = getTable("contact")
                .setTemplateType(CodegenTemplateTypeEnum.SUB.getType())
                .setFrontType(CodegenFrontTypeEnum.VUE3.getType())
                .setSubJoinColumnId(100L).setSubJoinMany(true);
        List<CodegenColumnDO> contactColumns = getColumnList("contact");
        // 准备参数（班主任）
        CodegenTableDO teacherTable = getTable("teacher")
                .setTemplateType(CodegenTemplateTypeEnum.SUB.getType())
                .setFrontType(CodegenFrontTypeEnum.VUE3.getType())
                .setSubJoinColumnId(200L).setSubJoinMany(false);
        List<CodegenColumnDO> teacherColumns = getColumnList("teacher");

        // 调用
        Map<String, String> result = codegenEngine.execute(table, columns,
                Arrays.asList(contactTable, teacherTable), Arrays.asList(contactColumns, teacherColumns));
        // 断言
        assertResult(result, path);
//        writeResult(result, "/root/ruoyi-vue-pro/yudao-module-infra/yudao-module-infra-biz/src/test/resources/" + path);
//        writeFile(result, "/Users/yunai/test/demo11.zip");
    }

    private static CodegenTableDO getTable(String name) {
        String content = ResourceUtil.readUtf8Str("codegen/table/" + name + ".json");
        return JsonUtils.parseObject(content, "table", CodegenTableDO.class);
    }

    private static List<CodegenColumnDO> getColumnList(String name) {
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

    @SuppressWarnings("rawtypes")
    private static void assertResult(Map<String, String> result, String path) {
        String assertContent = ResourceUtil.readUtf8Str(path + "/assert.json");
        List<HashMap> asserts = JsonUtils.parseArray(assertContent, HashMap.class);
        assertEquals(asserts.size(), result.size());
        // 校验每个文件
        asserts.forEach(assertMap -> {
            String contentPath = (String) assertMap.get("contentPath");
            String filePath = (String) assertMap.get("filePath");
            String content = ResourceUtil.readUtf8Str(path + "/" + contentPath);
            assertEquals(content, result.get(filePath), filePath + "：不匹配");
        });
    }

    // ==================== 调试专用 ====================

    /**
     * 【调试使用】将生成的代码，写入到文件
     *
     * @param result 生成的代码
     * @param path 写入文件的路径
     */
    private void writeFile(Map<String, String> result, String path) {
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
     * @param result 生成的代码
     * @param basePath 写入文件的路径（绝对路径）
     */
    private void writeResult(Map<String, String> result, String basePath) {
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
        FileUtil.writeUtf8String(JsonUtils.toJsonPrettyString(asserts), basePath +"/assert.json");
    }

}
