package cn.iocoder.yudao.module.infra.service.codegen.inner;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.biz.system.dict.DictDataCommonApi;
import cn.iocoder.yudao.framework.common.biz.system.dict.dto.DictDataRespDTO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.json.JsonUtils;
import cn.iocoder.yudao.framework.common.util.json.databind.NumberSerializer;
import cn.iocoder.yudao.framework.dict.core.DictFrameworkUtils;
import cn.iocoder.yudao.framework.excel.core.util.ExcelUtils;
import cn.iocoder.yudao.framework.mybatis.core.handler.DefaultDBFieldHandler;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.infra.dal.dataobject.codegen.CodegenColumnDO;
import cn.iocoder.yudao.module.infra.dal.dataobject.codegen.CodegenTableDO;
import cn.iocoder.yudao.module.infra.enums.codegen.CodegenFrontTypeEnum;
import cn.iocoder.yudao.module.infra.enums.codegen.CodegenSceneEnum;
import cn.iocoder.yudao.module.infra.enums.codegen.CodegenTemplateTypeEnum;
import cn.iocoder.yudao.module.infra.enums.codegen.CodegenVOTypeEnum;
import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.MybatisSqlSessionFactoryBuilder;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.core.toolkit.GlobalConfigUtils;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.apache.fesod.sheet.FesodSheet;
import org.apache.ibatis.datasource.unpooled.UnpooledDataSource;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;

import javax.sql.DataSource;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link CodegenEngine} 的集合（多选 checkbox）字段测试
 *
 * 集合字段使用 JSON 数组存储：javaType 保留元素类型，生成 List&lt;T&gt;，通过 JacksonTypeHandler 转换。包括：
 * 1. 后端、前端：断言生成的代码
 * 2. 运行时：将真实生成的 DO、Mapper、VO 代码动态编译，基于 H2 + MyBatis Plus 验证 JSON 数组的保存、读取、更新、查询，
 * 以及基于真实 Excel 文件的导入、导出
 *
 * @author 芋道源码
 */
public class CodegenEngineCollectionTest extends CodegenEngineAbstractTest {

    private static final String JAVA_SOURCE_ROOT = "/src/main/java/";

    /**
     * 超过 JavaScript 安全整数范围的 Long，序列化为字符串
     */
    private static final long BIG_LONG = 9007199254740993L;

    @TempDir
    Path tempDir;

    // ==================== 后端 ====================

    @Test
    public void testExecute_backend_vo() {
        // 准备参数
        CodegenTableDO table = getTable("student").setFrontType(CodegenFrontTypeEnum.VUE3_ELEMENT_PLUS.getType())
                .setTemplateType(CodegenTemplateTypeEnum.ONE.getType());
        List<CodegenColumnDO> columns = buildCollectionColumns(getColumnList("student"));

        // 调用
        Map<String, String> result = codegenEngine.execute(DbType.MYSQL, table, columns, null, null);
        assertNoUnresolvedReference(result);
        exportResult(result, "backend_vo");

        // 断言：DO 开启 autoResultMap，集合字段使用 JacksonTypeHandler + List<T>
        String doContent = findFile(result, "/InfraStudentDO.java");
        assertTrue(doContent.contains("import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;"));
        assertTrue(doContent.contains("@TableName(value = \"infra_student\", autoResultMap = true)"));
        assertTrue(doContent.contains("@TableField(typeHandler = JacksonTypeHandler.class)\n    private List<String> tags;"));
        assertTrue(doContent.contains("private List<Integer> levels;"));
        assertTrue(doContent.contains("private List<Long> refIds;"));
        assertTrue(doContent.contains("private List<Long> scores;"));
        assertTrue(doContent.contains("private Integer sex;"));
        // 断言：SaveReqVO 必填集合使用 @NotEmpty；仅展示的集合字段不在表单中
        String saveReqVO = findFile(result, "/InfraStudentSaveReqVO.java");
        assertTrue(saveReqVO.contains("@NotEmpty(message = \"标签不能为空\")\n    private List<String> tags;"));
        assertFalse(saveReqVO.contains("等级不能为空"));
        assertFalse(saveReqVO.contains("scores"));
        // 断言：RespVO 的 Excel 转换器：设置字典使用 MultiDictConvert；未设置字典使用 JsonConvert（仅导出）
        String respVO = findFile(result, "/InfraStudentRespVO.java");
        assertTrue(respVO.contains("@ExcelProperty(value = \"标签\", converter = MultiDictConvert.class)\n"
                + "    @DictFormat(\"system_user_sex\")"));
        assertTrue(respVO.contains("@ExcelProperty(value = \"分数\", converter = JsonConvert.class)\n"
                + "    private List<Long> scores;"));
        // 断言：PageReqVO 的筛选值为单个元素
        String pageReqVO = findFile(result, "/InfraStudentPageReqVO.java");
        assertTrue(pageReqVO.contains("private String tags;"));
        assertTrue(pageReqVO.contains("private Long refIds;"));
        // 断言：Mapper 的查询条件：= 为包含该元素；!= 为空或不包含该元素
        String mapper = findFile(result, "/InfraStudentMapper.java");
        assertTrue(mapper.contains(
                "                .and(ObjectUtil.isNotEmpty(reqVO.getTags()), jsonArrayContains(InfraStudentDO::getTags, reqVO.getTags()))\n"
                + "                .and(ObjectUtil.isNotEmpty(reqVO.getLevels()), jsonArrayContains(InfraStudentDO::getLevels, reqVO.getLevels()))\n"
                + "                .and(ObjectUtil.isNotEmpty(reqVO.getRefIds()), w -> w.isNull(InfraStudentDO::getRefIds)\n"
                + "                        .or().not(jsonArrayContains(InfraStudentDO::getRefIds, reqVO.getRefIds())))\n"
                + "                .orderByDesc(InfraStudentDO::getId));"), mapper);
        assertTrue(mapper.contains("private static Consumer<LambdaQueryWrapper<InfraStudentDO>> jsonArrayContains("
                + "SFunction<InfraStudentDO, ?> column, Object value) {"));
        assertTrue(mapper.contains(".likeIfPresent(InfraStudentDO::getName, reqVO.getName())"));
        // 断言：H2 建表使用 varchar
        String h2 = findFile(result, "sql/h2.sql");
        assertTrue(h2.contains("\"tags\" varchar NOT NULL,"));
        assertTrue(h2.contains("\"ref_ids\" varchar,"));
    }

    @Test
    public void testExecute_backend_do() {
        // 准备参数：DO 模式
        codegenProperties.setVoType(CodegenVOTypeEnum.DO.getType());
        codegenEngine.initGlobalBindingMap();
        CodegenTableDO table = getTable("student").setFrontType(CodegenFrontTypeEnum.VUE3_ELEMENT_PLUS.getType())
                .setTemplateType(CodegenTemplateTypeEnum.ONE.getType());
        List<CodegenColumnDO> columns = buildCollectionColumns(getColumnList("student"));

        // 调用
        Map<String, String> result = codegenEngine.execute(DbType.MYSQL, table, columns, null, null);
        assertNoUnresolvedReference(result);
        exportResult(result, "backend_do");

        // 断言：DO 同时承担 VO 的 Excel 注解
        String doContent = findFile(result, "/InfraStudentDO.java");
        assertTrue(doContent.contains("@ExcelProperty(value = \"标签\", converter = MultiDictConvert.class)\n"
                + "    @DictFormat(\"system_user_sex\") // TODO 代码优化：建议设置到对应的 DictTypeConstants 枚举类中\n"
                + "    @TableField(typeHandler = JacksonTypeHandler.class)\n"
                + "    private List<String> tags;"));
        assertTrue(doContent.contains("@ExcelProperty(value = \"分数\", converter = JsonConvert.class)\n"
                + "    @TableField(typeHandler = JacksonTypeHandler.class)\n"
                + "    private List<Long> scores;"));
    }

    @Test
    public void testExecute_backend_importEnable() {
        // 准备参数
        codegenProperties.setImportEnable(true);
        codegenEngine.initGlobalBindingMap();
        CodegenTableDO table = getTable("student").setFrontType(CodegenFrontTypeEnum.VUE3_ELEMENT_PLUS.getType())
                .setTemplateType(CodegenTemplateTypeEnum.ONE.getType());
        List<CodegenColumnDO> columns = buildCollectionColumns(getColumnList("student"));

        // 调用
        Map<String, String> result = codegenEngine.execute(DbType.MYSQL, table, columns, null, null);
        assertNoUnresolvedReference(result);
        exportResult(result, "backend_import");

        // 断言：设置字典的集合字段，使用 MultiDictConvert 双向转换
        String importVO = findFile(result, "/InfraStudentImportExcelVO.java");
        assertTrue(importVO.contains("import cn.iocoder.yudao.framework.excel.core.convert.MultiDictConvert;"));
        assertTrue(importVO.contains("@ExcelProperty(value = \"标签\", converter = MultiDictConvert.class)"));
        assertTrue(importVO.contains("private List<String> tags;"));
        assertTrue(importVO.contains("private List<Long> refIds;"));
        assertFalse(importVO.contains("scores"));
    }

    @ParameterizedTest
    @EnumSource(value = CodegenTemplateTypeEnum.class, names = {"MASTER_NORMAL", "MASTER_ERP", "MASTER_INNER"})
    public void testExecute_backend_subTable(CodegenTemplateTypeEnum templateType) {
        // 调用：主表无集合字段，子表有集合字段
        Map<String, String> result = generateSubTable(CodegenFrontTypeEnum.VUE3_VBEN5_ANTD_GENERAL, templateType);

        // 断言：主表、子表分别判断 autoResultMap
        String studentDO = findFile(result, "/InfraStudentDO.java");
        assertTrue(studentDO.contains("@TableName(\"infra_student\")"));
        assertFalse(studentDO.contains("JacksonTypeHandler"));
        String contactDO = findFile(result, "/InfraStudentContactDO.java");
        assertTrue(contactDO.contains("@TableName(value = \"infra_student_contact\", autoResultMap = true)"));
        assertTrue(contactDO.contains("@TableField(typeHandler = JacksonTypeHandler.class)\n    private List<String> tags;"));
        assertTrue(contactDO.contains("@TableField(typeHandler = JacksonTypeHandler.class)\n    private List<Long> refIds;"));
        String teacherDO = findFile(result, "/InfraStudentTeacherDO.java");
        assertTrue(teacherDO.contains("@TableName(value = \"infra_student_teacher\", autoResultMap = true)"));
        assertTrue(teacherDO.contains("@TableField(typeHandler = JacksonTypeHandler.class)\n    private List<Integer> levels;"));
    }

    // ==================== 前端 ====================

    @ParameterizedTest
    @EnumSource(value = CodegenFrontTypeEnum.class, names = {"VUE3_VBEN2_ANTD_SCHEMA"}, mode = EnumSource.Mode.EXCLUDE)
    public void testExecute_frontend_one(CodegenFrontTypeEnum frontType) {
        // 准备参数
        CodegenTableDO table = getTable("student").setFrontType(frontType.getType())
                .setTemplateType(CodegenTemplateTypeEnum.ONE.getType());
        List<CodegenColumnDO> columns = buildCollectionColumns(getColumnList("student"));

        // 调用
        Map<String, String> result = codegenEngine.execute(DbType.MYSQL, table, columns, null, null);
        assertNoUnresolvedReference(result);
        exportResult(result, "one_" + frontType.name());

        // 断言：回显时 null 归一化为空数组；Long 集合使用字符串，避免精度丢失
        switch (frontType) {
            case VUE2_ELEMENT_UI: {
                String form = findFile(result, "/StudentForm.vue");
                assertTrue(form.contains("res.data.tags = res.data.tags || [];"));
                break;
            }
            case VUE3_ELEMENT_PLUS: {
                String api = findFile(result, "/index.ts");
                assertTrue(api.contains(": string[]; // 标签"));
                assertTrue(api.contains(": number[]; // 等级"));
                assertTrue(api.contains(": (number | string)[]; // 关联编号"));
                String form = findFile(result, "/StudentForm.vue");
                // 注意：tags 是最后一个必填规则，末尾逗号会被 prettyCode 去除
                assertTrue(form.contains("tags: [{ required: true, type: 'array', message: '标签不能为空', trigger: 'change' }]"));
                assertTrue(form.contains("formData.value.tags = formData.value.tags ?? []"));
                assertTrue(form.contains("formData.value.refIds = (formData.value.refIds ?? []).map(String)"));
                assertTrue(form.contains("getStrDictOptions(DICT_TYPE.SYSTEM_USER_SEX)"));
                break;
            }
            case VUE3_VBEN5_ANTD_SCHEMA:
            case VUE3_VBEN5_ANTDV_NEXT_SCHEMA:
            case VUE3_VBEN5_EP_SCHEMA: {
                String data = findFile(result, "/data.ts");
                assertTrue(data.contains("defaultValue: [],"));
                assertTrue(data.contains("getDictLabel(DICT_TYPE.SYSTEM_USER_SEX, value)"));
                assertTrue(data.contains("getDictOptions(DICT_TYPE.SYSTEM_USER_SEX, 'string')"));
                String form = findFile(result, "/modules/form.vue");
                assertTrue(form.contains("tags ??= []"));
                assertTrue(form.contains("formData.value.refIds = (formData.value.refIds ?? []).map(String);"));
                break;
            }
            case VUE3_VBEN5_ANTD_GENERAL:
            case VUE3_VBEN5_ANTDV_NEXT_GENERAL:
            case VUE3_VBEN5_EP_GENERAL: {
                String form = findFile(result, "/modules/form.vue");
                assertTrue(form.contains("type: 'array'"));
                assertTrue(form.contains("formData.value.tags ??= [];"));
                assertTrue(form.contains("formData.value.refIds = (formData.value.refIds ?? []).map(String);"));
                String index = findFile(result, "/demo/index.vue");
                assertTrue(index.contains("v-for=\"item in row.tags || []\""));
                break;
            }
            case VUE3_ADMIN_UNIAPP_WOT: {
                String form = findFile(result, "/form/index.vue");
                assertTrue(form.contains("<wd-checkbox-group v-model=\"formData.tags\">"));
                assertTrue(form.contains("  tags: [],"));
                assertTrue(form.contains("formData.value.tags ??= []"));
                assertTrue(form.contains("formData.value.refIds = (formData.value.refIds ?? []).map(String)"));
                String detail = findFile(result, "/detail/index.vue");
                assertTrue(detail.contains("formData?.scores?.join('、') || '-'"));
                break;
            }
            default:
                fail("未覆盖的前端类型：" + frontType);
        }
    }

    @ParameterizedTest
    @EnumSource(value = CodegenFrontTypeEnum.class, names = {"VUE3_VBEN2_ANTD_SCHEMA"}, mode = EnumSource.Mode.EXCLUDE)
    public void testExecute_frontend_masterNormal(CodegenFrontTypeEnum frontType) {
        // 调用
        Map<String, String> result = generateSubTable(frontType, CodegenTemplateTypeEnum.MASTER_NORMAL);
        exportResult(result, "master_normal_" + frontType.name());

        // 断言：子表数据回显时，null 归一化为空数组；Long 集合转换为字符串
        switch (frontType) {
            case VUE2_ELEMENT_UI: {
                String contactForm = findFile(result, "/components/StudentContactForm.vue");
                assertTrue(contactForm.contains("row.tags = row.tags || [];"));
                break;
            }
            case VUE3_ELEMENT_PLUS: {
                String contactForm = findFile(result, "/components/StudentContactForm.vue");
                assertTrue(contactForm.contains("row.tags = row.tags ?? []"));
                assertTrue(contactForm.contains("row.refIds = (row.refIds ?? []).map(String)"));
                String teacherForm = findFile(result, "/components/StudentTeacherForm.vue");
                assertTrue(teacherForm.contains("formData.value.levels = formData.value.levels ?? []"));
                break;
            }
            case VUE3_VBEN5_ANTD_GENERAL:
            case VUE3_VBEN5_ANTDV_NEXT_GENERAL:
            case VUE3_VBEN5_EP_GENERAL:
            case VUE3_VBEN5_ANTD_SCHEMA:
            case VUE3_VBEN5_ANTDV_NEXT_SCHEMA:
            case VUE3_VBEN5_EP_SCHEMA: {
                String contactForm = findFile(result, "/student-contact-form.vue");
                assertTrue(contactForm.contains("row.tags ??= [];"));
                assertTrue(contactForm.contains("row.refIds = (row.refIds ?? []).map(String);"));
                String teacherForm = findFile(result, "/student-teacher-form.vue");
                assertTrue(teacherForm.contains("levels ??= [];"));
                break;
            }
            case VUE3_ADMIN_UNIAPP_WOT: {
                String form = findFile(result, "/demo/form/index.vue");
                assertTrue(form.contains("<wd-checkbox-group v-model=\"item.tags\">"));
                assertTrue(form.contains("item.tags ??= []"));
                assertTrue(form.contains("item.refIds = (item.refIds ?? []).map(String)"));
                assertTrue(form.contains("(Array.isArray(value) && value.length === 0)"));
                String detail = findFile(result, "/demo/detail/index.vue");
                assertTrue(detail.contains("标签：<dict-tag :type=\"DICT_TYPE.SYSTEM_USER_SEX\" :value=\"item.tags\" />"));
                break;
            }
            default:
                fail("未覆盖的前端类型：" + frontType);
        }
    }

    @ParameterizedTest
    @EnumSource(value = CodegenFrontTypeEnum.class, names = {"VUE3_VBEN2_ANTD_SCHEMA"}, mode = EnumSource.Mode.EXCLUDE)
    public void testExecute_frontend_masterErpAndInner(CodegenFrontTypeEnum frontType) {
        // 调用：ERP 模式，子表独立表单
        Map<String, String> erpResult = generateSubTable(frontType, CodegenTemplateTypeEnum.MASTER_ERP);
        exportResult(erpResult, "master_erp_" + frontType.name());
        // 断言
        if (frontType == CodegenFrontTypeEnum.VUE3_ADMIN_UNIAPP_WOT) {
            String contactForm = findFile(erpResult, "/student-contact/form/index.vue");
            assertTrue(contactForm.contains("<wd-checkbox-group v-model=\"formData.tags\">"));
            assertTrue(contactForm.contains("formData.value.tags ??= []"));
        } else if (frontType.name().startsWith("VUE3_VBEN5")) {
            String contactForm = findFile(erpResult, "/student-contact-form.vue");
            assertTrue(contactForm.contains("tags ??= []"));
        }

        // 调用：内嵌模式，仅校验可以正常渲染
        exportResult(generateSubTable(frontType, CodegenTemplateTypeEnum.MASTER_INNER), "master_inner_" + frontType.name());
    }

    // ==================== 运行时：H2 ====================

    @ParameterizedTest
    @EnumSource(CodegenVOTypeEnum.class)
    public void testExecute_runtime_h2(CodegenVOTypeEnum voType) throws Exception {
        // 准备参数
        codegenProperties.setVoType(voType.getType()).setImportEnable(true);
        codegenEngine.initGlobalBindingMap();
        CodegenTableDO table = new CodegenTableDO().setId(1L).setScene(CodegenSceneEnum.ADMIN.getScene())
                .setTableName("infra_codegen_collection").setTableComment("代码生成集合字段测试表")
                .setModuleName("infra").setBusinessName("collectiontest")
                .setClassName("InfraCodegenCollection").setClassComment("集合字段测试").setAuthor("芋道源码")
                .setFrontType(CodegenFrontTypeEnum.VUE3_VBEN5_ANTD_GENERAL.getType())
                .setTemplateType(CodegenTemplateTypeEnum.ONE.getType());

        // 调用：生成代码，并编译 DO、Mapper、VO
        Map<String, String> result = codegenEngine.execute(DbType.MYSQL, table, buildRuntimeColumns(), null, null);
        Map<String, String> sources = filterJavaSources(result);
        Path classesDir = compile(sources);
        String h2Sql = findFile(result, "sql/h2.sql");

        runWithClassLoader(classesDir, classLoader -> {
            Class<?> doClass = classLoader.loadClass(findClassName(sources, "/InfraCodegenCollectionDO.java"));
            Class<?> mapperClass = classLoader.loadClass(findClassName(sources, "/InfraCodegenCollectionMapper.java"));
            Class<?> pageReqVOClass = classLoader.loadClass(findClassName(sources, "/InfraCodegenCollectionPageReqVO.java"));
            if (voType == CodegenVOTypeEnum.VO) {
                assertSaveReqVOJson(classLoader.loadClass(findClassName(sources, "/InfraCodegenCollectionSaveReqVO.java")));
            }
            // 断言：数据库的保存、读取、查询、更新
            DataSource dataSource = createDataSource(voType.name().toLowerCase(), h2Sql);
            try (SqlSession sqlSession = createSqlSessionFactory(dataSource, mapperClass).openSession(true)) {
                @SuppressWarnings("unchecked")
                BaseMapperX<Object> mapper = (BaseMapperX<Object>) sqlSession.getMapper(mapperClass);
                assertCrudAndQuery(mapper, doClass, mapperClass.getMethod("selectPage", pageReqVOClass),
                        pageReqVOClass, dataSource);
            }
            // 断言：基于真实 Excel 文件的导入、导出
            Class<?> importExcelVOClass = classLoader.loadClass(findClassName(sources, "/InfraCodegenCollectionImportExcelVO.java"));
            Class<?> exportClass = voType == CodegenVOTypeEnum.VO
                    ? classLoader.loadClass(findClassName(sources, "/InfraCodegenCollectionRespVO.java")) : doClass;
            runWithDictData(() -> assertExcel(importExcelVOClass, exportClass));
        });
    }

    /**
     * 断言：JSON 数组的保存、读取，Mapper 生成的“包含该元素”“不包含该元素”查询，以及更新
     */
    private void assertCrudAndQuery(BaseMapperX<Object> mapper, Class<?> doClass, Method selectPage,
                                    Class<?> pageReqVOClass, DataSource dataSource) throws Exception {
        // 新增
        Object a = newDO(doClass, "a", List.of("1", "2"), List.of(1, 12), List.of(1L, BIG_LONG));
        Object b = newDO(doClass, "b", List.of("2"), Collections.emptyList(), null);
        Object c = newDO(doClass, "c", List.of("12"), null, List.of(2L));
        mapper.insert(a);
        mapper.insert(b);
        mapper.insert(c);
        Long idA = (Long) get(a, "id");
        Long idB = (Long) get(b, "id");

        // 断言：数据库存储为 JSON 数组字符串；null 存储为 NULL，[] 存储为 "[]"
        assertEquals("[\"1\",\"2\"]", selectRaw(dataSource, idA, "tags"));
        assertEquals("[1,\"" + BIG_LONG + "\"]", selectRaw(dataSource, idA, "ref_ids"));
        assertEquals("[]", selectRaw(dataSource, idB, "levels"));
        assertNull(selectRaw(dataSource, idB, "ref_ids"));
        // 断言：读取时转换为 List，元素类型正确，超过 JavaScript 安全整数的 Long 不丢失精度
        Object dbA = mapper.selectById(idA);
        assertEquals(List.of("1", "2"), get(dbA, "tags"));
        assertEquals(List.of(1, 12), get(dbA, "levels"));
        assertEquals(List.of(1L, BIG_LONG), get(dbA, "refIds"));
        assertEquals(Collections.emptyList(), get(mapper.selectById(idB), "levels"));

        // 断言：= 包含该元素，按元素边界匹配，"1" 不会匹配 "12"
        assertEquals(Set.of("a"), selectNames(mapper, selectPage, pageReqVOClass, "tags", "1"));
        assertEquals(Set.of("a", "b"), selectNames(mapper, selectPage, pageReqVOClass, "tags", "2"));
        assertEquals(Set.of("c"), selectNames(mapper, selectPage, pageReqVOClass, "tags", "12"));
        assertEquals(Set.of("a"), selectNames(mapper, selectPage, pageReqVOClass, "levels", 12));
        // 断言：!= 为空，或不包含该元素
        assertEquals(Set.of("b", "c"), selectNames(mapper, selectPage, pageReqVOClass, "refIds", BIG_LONG));
        assertEquals(Set.of("a", "b"), selectNames(mapper, selectPage, pageReqVOClass, "refIds", 2L));
        // 断言：未传筛选值时不过滤
        assertEquals(Set.of("a", "b", "c"), selectNames(mapper, selectPage, pageReqVOClass, null, null));

        // 更新：清空为 []；null 不覆盖（MyBatis Plus 默认 NOT_NULL 更新策略）
        Object updateA = newInstance(doClass);
        set(updateA, "id", idA);
        set(updateA, "levels", Collections.emptyList());
        mapper.updateById(updateA);
        assertEquals("[]", selectRaw(dataSource, idA, "levels"));
        assertEquals("[1,\"" + BIG_LONG + "\"]", selectRaw(dataSource, idA, "ref_ids"));
    }

    /**
     * 断言：SaveReqVO 按 JSON 数组反序列化为对应元素类型的 List；必填集合使用 @NotEmpty
     */
    private static void assertSaveReqVOJson(Class<?> saveReqVOClass) throws Exception {
        Object reqVO = JsonUtils.parseObject("{\"name\":\"a\",\"tags\":[\"1\"],\"levels\":[1,2],\"refIds\":[\""
                + BIG_LONG + "\",3]}", saveReqVOClass);
        assertEquals(List.of("1"), get(reqVO, "tags"));
        assertEquals(List.of(1, 2), get(reqVO, "levels"));
        assertEquals(List.of(BIG_LONG, 3L), get(reqVO, "refIds"));
        assertNotNull(saveReqVOClass.getDeclaredField("tags").getAnnotation(jakarta.validation.constraints.NotEmpty.class));
    }

    /**
     * 断言：Excel 导入、导出
     *
     * 1. 设置字典：MultiDictConvert，Excel 中为“、”分隔的字典标签，导入后元素的值、类型不变
     * 2. 未设置字典：JsonConvert 导出 JSON 数组
     */
    private static void assertExcel(Class<?> importExcelVOClass, Class<?> exportClass) throws Exception {
        // 导入：写入真实的 Excel 文件后，按照项目的导入方式读取
        Object row = newInstance(importExcelVOClass);
        set(row, "name", "a");
        set(row, "tags", List.of("1", "2"));
        set(row, "levels", List.of(2));
        set(row, "refIds", List.of(1L, BIG_LONG));
        byte[] excel = writeExcel(importExcelVOClass, List.of(row));
        assertEquals("男、女", readExcelRows(excel).get(0).get("标签"));
        List<?> list = ExcelUtils.read(new MockMultipartFile("file", "import.xlsx", null, excel), importExcelVOClass);
        assertEquals(1, list.size());
        assertEquals(List.of("1", "2"), get(list.get(0), "tags"));
        assertEquals(List.of(2), get(list.get(0), "levels"));
        assertEquals(List.of(1L, BIG_LONG), get(list.get(0), "refIds"));

        // 导出
        Object export = newInstance(exportClass);
        set(export, "name", "a");
        set(export, "refIds", List.of(BIG_LONG));
        set(export, "scores", List.of(1L, BIG_LONG));
        Map<String, String> exportRow = readExcelRows(writeExcel(exportClass, List.of(export))).get(0);
        assertEquals("大号", exportRow.get("关联编号"));
        assertEquals("[1,\"" + BIG_LONG + "\"]", exportRow.get("分数"));
    }

    @SuppressWarnings("unchecked")
    private static Set<String> selectNames(BaseMapperX<Object> mapper, Method selectPage, Class<?> pageReqVOClass,
                                           String field, Object value) throws Exception {
        Object reqVO = newInstance(pageReqVOClass);
        if (field != null) {
            set(reqVO, field, value);
        }
        PageResult<Object> pageResult = (PageResult<Object>) selectPage.invoke(mapper, reqVO);
        Set<String> names = new LinkedHashSet<>();
        for (Object item : pageResult.getList()) {
            names.add((String) get(item, "name"));
        }
        assertEquals(names.size(), pageResult.getTotal().intValue());
        return names;
    }

    // ==================== 运行时：准备数据 ====================

    /**
     * 运行时的字段：
     *
     * 1. tags：String + 字典，必填，“=” 查询
     * 2. levels：Integer + 字典，非必填，“=” 查询
     * 3. refIds：Long + 字典，非必填，“!=” 查询
     * 4. scores：Long，未设置字典，仅列表展示（以及 Excel 导出）
     */
    private static List<CodegenColumnDO> buildRuntimeColumns() {
        List<CodegenColumnDO> columns = new ArrayList<>();
        columns.add(buildScalarColumn("id", "id", "Long", "input", "编号")
                .setPrimaryKey(true).setUpdateOperation(true).setListOperationResult(true));
        columns.add(buildScalarColumn("name", "name", "String", "input", "名字")
                .setNullable(false).setCreateOperation(true).setUpdateOperation(true)
                .setListOperation(true).setListOperationCondition("LIKE").setListOperationResult(true));
        columns.addAll(buildCollectionColumns(List.of()));
        // BaseDO 的字段：不会生成到 DO 中，但需要生成到 H2 建表语句中
        columns.add(buildScalarColumn("creator", "creator", "String", "input", "创建者"));
        columns.add(buildScalarColumn("create_time", "createTime", "LocalDateTime", "datetime", "创建时间")
                .setListOperation(true).setListOperationCondition("BETWEEN").setListOperationResult(true));
        columns.add(buildScalarColumn("updater", "updater", "String", "input", "更新者"));
        columns.add(buildScalarColumn("update_time", "updateTime", "LocalDateTime", "datetime", "更新时间"));
        columns.add(buildScalarColumn("deleted", "deleted", "Boolean", "radio", "是否删除"));
        return columns;
    }

    private static CodegenColumnDO buildScalarColumn(String columnName, String javaField, String javaType,
                                                     String htmlType, String comment) {
        return new CodegenColumnDO().setColumnName(columnName).setColumnComment(comment)
                .setJavaField(javaField).setJavaType(javaType).setHtmlType(htmlType).setDictType("")
                .setPrimaryKey(false).setNullable(true)
                .setCreateOperation(false).setUpdateOperation(false)
                .setListOperation(false).setListOperationResult(false);
    }

    private static Object newDO(Class<?> doClass, String name, List<String> tags, List<Integer> levels,
                                List<Long> refIds) throws Exception {
        Object object = newInstance(doClass);
        set(object, "name", name);
        set(object, "tags", tags);
        set(object, "levels", levels);
        set(object, "refIds", refIds);
        return object;
    }

    // ==================== 运行时：编译、数据库、Excel ====================

    /**
     * 获得需要编译的 Java 代码：DO、Mapper、VO
     */
    private static Map<String, String> filterJavaSources(Map<String, String> result) {
        return result.entrySet().stream()
                .filter(entry -> entry.getKey().endsWith(".java") && entry.getKey().contains(JAVA_SOURCE_ROOT))
                .filter(entry -> entry.getKey().contains("/dal/") || entry.getKey().contains("/vo/"))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, LinkedHashMap::new));
    }

    private Path compile(Map<String, String> sources) throws IOException {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        assertNotNull(compiler, "需要使用 JDK（而不是 JRE）运行该单测");
        Path sourceDir = Files.createDirectories(tempDir.resolve("src"));
        Path classesDir = Files.createDirectories(tempDir.resolve("classes"));
        // 与项目根目录的 lombok.config 保持一致
        Files.writeString(sourceDir.resolve("lombok.config"), "config.stopBubbling = true\n"
                + "lombok.tostring.callsuper=CALL\nlombok.equalsandhashcode.callsuper=CALL\nlombok.accessors.chain=true\n",
                StandardCharsets.UTF_8);
        List<File> files = new ArrayList<>();
        for (Map.Entry<String, String> entry : sources.entrySet()) {
            Path file = sourceDir.resolve(StrUtil.subAfter(entry.getKey(), JAVA_SOURCE_ROOT, false));
            Files.createDirectories(file.getParent());
            Files.writeString(file, entry.getValue(), StandardCharsets.UTF_8);
            files.add(file.toFile());
        }
        String classpath = resolveClasspath();
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        try (StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, StandardCharsets.UTF_8)) {
            List<String> options = Arrays.asList("-encoding", "UTF-8", "-nowarn", "-parameters",
                    "-classpath", classpath, "-processorpath", classpath,
                    "-processor", "lombok.launch.AnnotationProcessorHider$AnnotationProcessor",
                    "-d", classesDir.toString());
            Boolean success = compiler.getTask(null, fileManager, diagnostics, options, null,
                    fileManager.getJavaFileObjectsFromFiles(files)).call();
            assertTrue(Boolean.TRUE.equals(success), () -> "生成代码编译失败：\n" + diagnostics.getDiagnostics().stream()
                    .filter(diagnostic -> diagnostic.getKind() == Diagnostic.Kind.ERROR)
                    .map(Object::toString).collect(Collectors.joining("\n")));
        }
        return classesDir;
    }

    /**
     * 解析单测的 classpath
     *
     * Maven Surefire 可能使用 manifest-only jar 启动，所以需要展开 jar 中 Manifest 的 Class-Path
     */
    private static String resolveClasspath() {
        String classpath = System.getProperty("surefire.test.class.path");
        if (StrUtil.isBlank(classpath)) {
            classpath = System.getProperty("java.class.path");
        }
        Set<String> entries = new LinkedHashSet<>();
        for (String entry : classpath.split(File.pathSeparator)) {
            addClasspathEntry(entries, entry);
        }
        return String.join(File.pathSeparator, entries);
    }

    private static void addClasspathEntry(Set<String> entries, String entry) {
        if (StrUtil.isBlank(entry) || !entries.add(entry)) {
            return;
        }
        File file = new File(entry);
        if (!file.isFile() || !entry.endsWith(".jar")) {
            return;
        }
        try (JarFile jarFile = new JarFile(file)) {
            Manifest manifest = jarFile.getManifest();
            String manifestClasspath = manifest != null ? manifest.getMainAttributes().getValue(Attributes.Name.CLASS_PATH) : null;
            if (StrUtil.isBlank(manifestClasspath)) {
                return;
            }
            for (String item : manifestClasspath.trim().split("\\s+")) {
                URI uri = file.toURI().resolve(item);
                if ("file".equals(uri.getScheme())) {
                    addClasspathEntry(entries, new File(uri).getPath());
                }
            }
        } catch (IOException | IllegalArgumentException ignored) {
            // 忽略无法解析的 classpath 项
        }
    }

    /**
     * 使用编译后的类执行回调：模拟 YudaoJacksonAutoConfiguration + YudaoMybatisAutoConfiguration，
     * Long 超过安全范围时序列化为字符串；执行结束后，还原 JsonUtils、JacksonTypeHandler 的 ObjectMapper
     */
    private static void runWithClassLoader(Path classesDir, RuntimeCallback callback) throws Exception {
        ObjectMapper oldJsonUtilsMapper = JsonUtils.getObjectMapper();
        ObjectMapper oldTypeHandlerMapper = JacksonTypeHandler.getObjectMapper();
        ClassLoader oldContextClassLoader = Thread.currentThread().getContextClassLoader();
        try (URLClassLoader classLoader = new URLClassLoader(new URL[]{classesDir.toUri().toURL()},
                CodegenEngineCollectionTest.class.getClassLoader())) {
            Thread.currentThread().setContextClassLoader(classLoader);
            ObjectMapper objectMapper = oldJsonUtilsMapper.copy().registerModule(new SimpleModule()
                    .addSerializer(Long.class, NumberSerializer.INSTANCE)
                    .addSerializer(Long.TYPE, NumberSerializer.INSTANCE));
            JsonUtils.init(objectMapper);
            JacksonTypeHandler.setObjectMapper(objectMapper);
            callback.run(classLoader);
        } finally {
            JsonUtils.init(oldJsonUtilsMapper);
            JacksonTypeHandler.setObjectMapper(oldTypeHandlerMapper);
            Thread.currentThread().setContextClassLoader(oldContextClassLoader);
        }
    }

    /**
     * 使用测试字典数据执行回调；执行结束后，还原 DictFrameworkUtils 的字典 API，并清空缓存
     */
    private static void runWithDictData(ThrowingRunnable runnable) throws Exception {
        Field field = DictFrameworkUtils.class.getDeclaredField("dictDataApi");
        field.setAccessible(true);
        DictDataCommonApi oldDictDataApi = (DictDataCommonApi) field.get(null);
        List<DictDataRespDTO> dictDatas = List.of(
                new DictDataRespDTO().setDictType("system_user_sex").setValue("1").setLabel("男").setStatus(0),
                new DictDataRespDTO().setDictType("system_user_sex").setValue("2").setLabel("女").setStatus(0),
                new DictDataRespDTO().setDictType("system_user_sex").setValue(String.valueOf(BIG_LONG)).setLabel("大号").setStatus(0));
        DictFrameworkUtils.init(dictType -> dictDatas);
        DictFrameworkUtils.clearCache();
        try {
            runnable.run();
        } finally {
            DictFrameworkUtils.init(oldDictDataApi);
            DictFrameworkUtils.clearCache();
        }
    }

    /**
     * 按照项目的导出方式（{@link ExcelUtils#write}），生成真实的 Excel 文件
     */
    @SuppressWarnings("unchecked")
    private static byte[] writeExcel(Class<?> head, List<?> data) throws IOException {
        MockHttpServletResponse response = new MockHttpServletResponse();
        ExcelUtils.write(response, "codegen.xls", "数据", (Class<Object>) head, (List<Object>) data);
        return response.getContentAsByteArray();
    }

    /**
     * 读取 Excel 文件的单元格原文，按照表头转换为 Map
     */
    private static List<Map<String, String>> readExcelRows(byte[] excel) {
        List<Map<Integer, String>> rows = FesodSheet.read(new ByteArrayInputStream(excel)).sheet()
                .headRowNumber(0).doReadSync();
        Map<Integer, String> head = rows.get(0);
        List<Map<String, String>> result = new ArrayList<>();
        for (Map<Integer, String> row : rows.subList(1, rows.size())) {
            Map<String, String> item = new HashMap<>();
            head.forEach((index, name) -> item.put(name, row.get(index)));
            result.add(item);
        }
        return result;
    }

    private static DataSource createDataSource(String name, String sql) throws Exception {
        String url = "jdbc:h2:mem:codegen_collection_" + name
                + ";MODE=MYSQL;DATABASE_TO_UPPER=false;NON_KEYWORDS=value;DB_CLOSE_DELAY=-1";
        UnpooledDataSource dataSource = new UnpooledDataSource("org.h2.Driver", url, "sa", "");
        // 执行建表语句（去除注释行）
        String statements = Arrays.stream(sql.split("\n")).filter(line -> !line.trim().startsWith("--"))
                .collect(Collectors.joining("\n"));
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
            for (String item : statements.split(";")) {
                if (StrUtil.isNotBlank(item)) {
                    statement.execute(item);
                }
            }
        }
        return dataSource;
    }

    private static SqlSessionFactory createSqlSessionFactory(DataSource dataSource, Class<?> mapperClass) {
        MybatisConfiguration configuration = new MybatisConfiguration();
        configuration.setMapUnderscoreToCamelCase(true);
        configuration.setEnvironment(new Environment("codegen-collection-test", new JdbcTransactionFactory(), dataSource));
        // 与项目一致：MySQL 等数据库使用 AUTO 自增主键；BaseDO 字段自动填充
        GlobalConfig globalConfig = GlobalConfigUtils.defaults().setBanner(false)
                .setDbConfig(new GlobalConfig.DbConfig().setIdType(IdType.AUTO))
                .setMetaObjectHandler(new DefaultDBFieldHandler());
        GlobalConfigUtils.setGlobalConfig(configuration, globalConfig);
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.H2));
        configuration.addInterceptor(interceptor);
        configuration.addMapper(mapperClass);
        return new MybatisSqlSessionFactoryBuilder().build(configuration);
    }

    private static String selectRaw(DataSource dataSource, Long id, String column) throws Exception {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT " + column + " FROM infra_codegen_collection WHERE id = ?")) {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                assertTrue(resultSet.next());
                return resultSet.getString(1);
            }
        }
    }

    @FunctionalInterface
    private interface RuntimeCallback {

        void run(ClassLoader classLoader) throws Exception;

    }

    @FunctionalInterface
    private interface ThrowingRunnable {

        void run() throws Exception;

    }

    // ==================== 运行时：反射工具 ====================

    private static String findClassName(Map<String, String> sources, String suffix) {
        String path = sources.keySet().stream().filter(key -> key.endsWith(suffix)).findFirst()
                .orElseThrow(() -> new AssertionError("缺少生成文件：" + suffix));
        return StrUtil.removeSuffix(StrUtil.subAfter(path, JAVA_SOURCE_ROOT, false), ".java").replace('/', '.');
    }

    private static Object newInstance(Class<?> clazz) throws Exception {
        return clazz.getDeclaredConstructor().newInstance();
    }

    private static void set(Object object, String field, Object value) throws Exception {
        String methodName = "set" + StrUtil.upperFirst(field);
        for (Method method : object.getClass().getMethods()) {
            if (method.getName().equals(methodName) && method.getParameterCount() == 1) {
                method.invoke(object, value);
                return;
            }
        }
        fail(object.getClass().getName() + " 缺少方法：" + methodName);
    }

    private static Object get(Object object, String field) throws Exception {
        return object.getClass().getMethod("get" + StrUtil.upperFirst(field)).invoke(object);
    }

    // ==================== 辅助方法 ====================

    /**
     * 生成主子表：主表（学生）无集合字段；子表（联系人，一对多）、子表（班主任，一对一）有集合字段
     *
     * 1. 联系人 tags：String + 字典，必填，“!=” 查询
     * 2. 联系人 refIds：Long + 字典，非必填，“=” 查询
     * 3. 班主任 levels：Integer + 字典，必填
     */
    private Map<String, String> generateSubTable(CodegenFrontTypeEnum frontType, CodegenTemplateTypeEnum templateType) {
        CodegenTableDO table = getTable("student").setFrontType(frontType.getType())
                .setTemplateType(templateType.getType());
        CodegenTableDO contact = getTable("contact").setFrontType(frontType.getType())
                .setTemplateType(CodegenTemplateTypeEnum.SUB.getType()).setSubJoinColumnId(100L).setSubJoinMany(true);
        CodegenTableDO teacher = getTable("teacher").setFrontType(frontType.getType())
                .setTemplateType(CodegenTemplateTypeEnum.SUB.getType()).setSubJoinColumnId(200L).setSubJoinMany(false);
        List<CodegenColumnDO> contactColumns = getColumnList("contact");
        contactColumns.add(buildColumn("tags", "tags", "String", "system_user_sex", "标签", false)
                .setListOperation(true).setListOperationCondition("!="));
        contactColumns.add(buildColumn("ref_ids", "refIds", "Long", "system_user_sex", "关联编号", true)
                .setListOperation(true).setListOperationCondition("="));
        List<CodegenColumnDO> teacherColumns = getColumnList("teacher");
        teacherColumns.add(buildColumn("levels", "levels", "Integer", "system_user_sex", "等级", false));
        Map<String, String> result = codegenEngine.execute(DbType.MYSQL, table, getColumnList("student"),
                List.of(contact, teacher), List.of(contactColumns, teacherColumns));
        assertNoUnresolvedReference(result);
        return result;
    }

    /**
     * 构建包含集合字段的字段列表：
     *
     * 1. tags：String + 字典，必填，“=” 查询
     * 2. levels：Integer + 字典，非必填，“=” 查询
     * 3. refIds：Long + 字典，非必填，“!=” 查询
     * 4. scores：Long，未设置字典，仅列表展示（以及 Excel 导出）
     */
    private static List<CodegenColumnDO> buildCollectionColumns(List<CodegenColumnDO> columns) {
        List<CodegenColumnDO> result = new ArrayList<>(columns);
        result.add(buildColumn("tags", "tags", "String", "system_user_sex", "标签", false)
                .setListOperation(true).setListOperationCondition("="));
        result.add(buildColumn("levels", "levels", "Integer", "system_user_sex", "等级", true)
                .setListOperation(true).setListOperationCondition("="));
        result.add(buildColumn("ref_ids", "refIds", "Long", "system_user_sex", "关联编号", true)
                .setListOperation(true).setListOperationCondition("!="));
        result.add(buildColumn("scores", "scores", "Long", "", "分数", true)
                .setCreateOperation(false).setUpdateOperation(false));
        return result;
    }

    private static CodegenColumnDO buildColumn(String columnName, String javaField, String javaType, String dictType,
                                               String comment, boolean nullable) {
        return new CodegenColumnDO().setColumnName(columnName).setDataType("VARCHAR")
                .setJavaField(javaField).setJavaType(javaType).setDictType(dictType)
                .setColumnComment(comment).setHtmlType("checkbox").setNullable(nullable).setPrimaryKey(false)
                .setCreateOperation(true).setUpdateOperation(true)
                .setListOperation(false).setListOperationResult(true);
    }

    /**
     * 【调试 / 验收使用】导出真实生成结果
     *
     * 命令行加 {@code -Dcodegen.collectionOutput=/path/to/dir} 启用，供前端项目的类型检查等真实验证使用
     *
     * @param result 生成结果
     * @param name   导出目录名
     */
    private void exportResult(Map<String, String> result, String name) {
        String outputPath = System.getProperty("codegen.collectionOutput");
        if (outputPath != null && !outputPath.isBlank()) {
            writeResult(result, outputPath + "/" + name);
        }
    }

    /**
     * 断言生成结果中，没有未解析的 Velocity 变量（例如说模板中拼写错误的 $codegenHelper）
     */
    private static void assertNoUnresolvedReference(Map<String, String> result) {
        result.forEach((path, content) -> {
            assertFalse(content.contains("codegenHelper"), path + "：存在未解析的 codegenHelper");
            assertFalse(content.contains("$dictMethod") || content.contains("$collectionDictMethod"),
                    path + "：存在未解析的字典方法");
            assertFalse(content.contains("$dictType") || content.contains("DICT_TYPE.null"),
                    path + "：存在未解析的字典类型");
        });
    }

    private static String findFile(Map<String, String> result, String suffix) {
        return result.entrySet().stream().filter(entry -> entry.getKey().endsWith(suffix))
                .findFirst().orElseThrow(() -> new AssertionError("缺少生成文件：" + suffix)).getValue();
    }

}
