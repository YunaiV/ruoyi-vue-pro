package cn.iocoder.yudao.module.infra.service.codegen.inner;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.ZipUtil;
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
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
    public void testExecute_vue3_crud() {
        // 准备请求参数
        CodegenTableDO table = new CodegenTableDO().setScene(CodegenSceneEnum.ADMIN.getScene()).setParentMenuId(888L)
                .setTableName("infra_demo01_student").setTableComment("学生表")
                .setModuleName("infra").setBusinessName("demo01").setClassName("InfraDemo01Student")
                .setClassComment("学生").setAuthor("芋道源码")
                .setTemplateType(CodegenTemplateTypeEnum.ONE.getType())
                .setFrontType(CodegenFrontTypeEnum.VUE3.getType());
        CodegenColumnDO idColumn = new CodegenColumnDO().setColumnName("id").setDataType(JdbcType.BIGINT.name())
                .setColumnComment("编号").setNullable(false).setPrimaryKey(true).setAutoIncrement(true)
                .setJavaType("Long").setJavaField("id").setExample("1024")
                .setCreateOperation(false).setUpdateOperation(true)
                .setListOperation(false)
                .setListOperationResult(true);
        CodegenColumnDO nameColumn = new CodegenColumnDO().setColumnName("name").setDataType(JdbcType.VARCHAR.name())
                .setColumnComment("名字").setNullable(false)
                .setJavaType("String").setJavaField("name").setExample("芋头")
                .setCreateOperation(true).setUpdateOperation(true)
                .setListOperation(true).setListOperationCondition(CodegenColumnListConditionEnum.LIKE.getCondition())
                .setListOperationResult(true)
                .setHtmlType(CodegenColumnHtmlTypeEnum.INPUT.getType());
        CodegenColumnDO descriptionColumn = new CodegenColumnDO().setColumnName("description").setDataType(JdbcType.VARCHAR.name())
                .setColumnComment("简介").setNullable(false)
                .setJavaType("String").setJavaField("description").setExample("我是介绍")
                .setCreateOperation(true).setUpdateOperation(true)
                .setListOperation(false)
                .setListOperationResult(true)
                .setHtmlType(CodegenColumnHtmlTypeEnum.TEXTAREA.getType());
        CodegenColumnDO birthdayColumn = new CodegenColumnDO().setColumnName("birthday").setDataType(JdbcType.DATE.name())
                .setColumnComment("出生日期").setNullable(false)
                .setJavaType("LocalDateTime").setJavaField("birthday")
                .setCreateOperation(true).setUpdateOperation(true)
                .setListOperation(true).setListOperationCondition(CodegenColumnListConditionEnum.EQ.getCondition())
                .setListOperationResult(true)
                .setHtmlType(CodegenColumnHtmlTypeEnum.DATETIME.getType());
        CodegenColumnDO sexColumn = new CodegenColumnDO().setColumnName("sex").setDataType(JdbcType.INTEGER.name())
                .setColumnComment("性别").setNullable(false)
                .setJavaType("Integer").setJavaField("sex").setExample("1")
                .setCreateOperation(true).setUpdateOperation(true)
                .setListOperation(true).setListOperationCondition(CodegenColumnListConditionEnum.EQ.getCondition())
                .setListOperationResult(true)
                .setHtmlType(CodegenColumnHtmlTypeEnum.SELECT.getType())
                .setDictType("system_user_sex");
        CodegenColumnDO enabledColumn = new CodegenColumnDO().setColumnName("enabled").setDataType(JdbcType.BOOLEAN.name())
                .setColumnComment("是否有效").setNullable(false)
                .setJavaType("Boolean").setJavaField("enabled").setExample("true")
                .setCreateOperation(true).setUpdateOperation(true)
                .setListOperation(true).setListOperationCondition(CodegenColumnListConditionEnum.EQ.getCondition())
                .setListOperationResult(true)
                .setHtmlType(CodegenColumnHtmlTypeEnum.RADIO.getType())
                .setDictType("infra_boolean_string");
        CodegenColumnDO avatarColumn = new CodegenColumnDO().setColumnName("avatar").setDataType(JdbcType.VARCHAR.name())
                .setColumnComment("头像").setNullable(false)
                .setJavaType("String").setJavaField("avatar").setExample("https://www.iocoder.cn/1.png")
                .setCreateOperation(true).setUpdateOperation(true)
                .setListOperation(false)
                .setListOperationResult(true)
                .setHtmlType(CodegenColumnHtmlTypeEnum.IMAGE_UPLOAD.getType());
        CodegenColumnDO videoColumn = new CodegenColumnDO().setColumnName("video").setDataType(JdbcType.VARCHAR.name())
                .setColumnComment("附件").setNullable(true)
                .setJavaType("String").setJavaField("video").setExample("https://www.iocoder.cn/1.mp4")
                .setCreateOperation(true).setUpdateOperation(true)
                .setListOperation(false)
                .setListOperationResult(true)
                .setHtmlType(CodegenColumnHtmlTypeEnum.FILE_UPLOAD.getType());
        CodegenColumnDO memoColumn = new CodegenColumnDO().setColumnName("memo").setDataType(JdbcType.VARCHAR.name())
                .setColumnComment("备注").setNullable(false)
                .setJavaType("String").setJavaField("memo").setExample("我是备注")
                .setCreateOperation(true).setUpdateOperation(true)
                .setListOperation(false)
                .setListOperationResult(true)
                .setHtmlType(CodegenColumnHtmlTypeEnum.EDITOR.getType());
        CodegenColumnDO createTimeColumn = new CodegenColumnDO().setColumnName("create_time").setDataType(JdbcType.DATE.name())
                .setColumnComment("创建时间").setNullable(true)
                .setJavaType("LocalDateTime").setJavaField("createTime")
                .setCreateOperation(false).setUpdateOperation(false)
                .setListOperation(true).setListOperationCondition(CodegenColumnListConditionEnum.BETWEEN.getCondition())
                .setListOperationResult(true)
                .setHtmlType(CodegenColumnHtmlTypeEnum.DATETIME.getType());
        List<CodegenColumnDO> columns = Arrays.asList(idColumn, nameColumn, descriptionColumn, birthdayColumn,
                sexColumn, enabledColumn, avatarColumn, videoColumn, memoColumn, createTimeColumn);

        // 调用
        Map<String, String> result = codegenEngine.execute(table, columns, null, null);

        // 构建 zip 包
        writeFile(result, "/Users/yunai/test/demo01.zip");

        // 断言
        assertEquals(21, result.size());
        // 断言 vo 类
        for (String vo : new String[]{"SystemUserBaseVO", "SystemUserCreateReqVO", "SystemUserUpdateReqVO", "SystemUserRespVO",
                "SystemUserPageReqVO", "SystemUserExportReqVO", "SystemUserExcelVO"}) {
            assertPathContentEquals("vue3_crud/java/" + vo,
                    result, "yudao-module-system/yudao-module-system-biz/src/main/java/cn/iocoder/yudao/module/system/controller/admin/user/vo/" + vo + ".java");
        }
        // 断言 controller 类
        assertPathContentEquals("vue3_crud/java/SystemUserController",
                result, "yudao-module-system/yudao-module-system-biz/src/main/java/cn/iocoder/yudao/module/system/controller/admin/user/SystemUserController.java");
        // 断言 service 类
        assertPathContentEquals("vue3_crud/java/SystemUserService",
                result, "yudao-module-system/yudao-module-system-biz/src/main/java/cn/iocoder/yudao/module/system/service/user/SystemUserService.java");
        assertPathContentEquals("vue3_crud/java/SystemUserServiceImpl",
                result, "yudao-module-system/yudao-module-system-biz/src/main/java/cn/iocoder/yudao/module/system/service/user/SystemUserServiceImpl.java");
        // 断言 convert 类
        assertPathContentEquals("vue3_crud/java/SystemUserConvert",
                result, "yudao-module-system/yudao-module-system-biz/src/main/java/cn/iocoder/yudao/module/system/convert/user/SystemUserConvert.java");
        // 断言 enums 类
        assertPathContentEquals("vue3_crud/java/ErrorCodeConstants",
                result, "yudao-module-system/yudao-module-system-api/src/main/java/cn/iocoder/yudao/module/system/enums/ErrorCodeConstants_手动操作.java");
        // 断言 dal 类
        assertPathContentEquals("vue3_crud/java/SystemUserDO",
                result, "yudao-module-system/yudao-module-system-biz/src/main/java/cn/iocoder/yudao/module/system/dal/dataobject/user/SystemUserDO.java");
        assertPathContentEquals("vue3_crud/java/SystemUserMapper",
                result, "yudao-module-system/yudao-module-system-biz/src/main/java/cn/iocoder/yudao/module/system/dal/mysql/user/SystemUserMapper.java");
        assertPathContentEquals("vue3_crud/java/SystemUserMapper_xml",
                result, "yudao-module-system/yudao-module-system-biz/src/main/resources/mapper/user/SystemUserMapper.xml");
        // 断言 test 类
        assertPathContentEquals("vue3_crud/java/SystemUserServiceImplTest",
                result, "yudao-module-system/yudao-module-system-biz/src/test/java/cn/iocoder/yudao/module/system/service/user/SystemUserServiceImplTest.java");
        // 断言 sql 语句
        assertPathContentEquals("vue3_crud/sql/h2",
                result, "sql/h2.sql");
        assertPathContentEquals("vue3_crud/sql/sql",
                result, "sql/sql.sql");
        // 断言 vue 语句
        assertPathContentEquals("vue3_crud/vue/index",
                result, "yudao-ui-admin-vue3/src/views/system/user/index.vue");
        assertPathContentEquals("vue3_crud/vue/form",
                result, "yudao-ui-admin-vue3/src/views/system/user/UserForm.vue");
        assertPathContentEquals("vue3_crud/vue/api",
                result, "yudao-ui-admin-vue3/src/api/system/user/index.ts");
    }

    @Test
    public void testExecute_vue3_masterNormal() {
        // 准备请求参数
        // 主表
        CodegenTableDO table = new CodegenTableDO().setScene(CodegenSceneEnum.ADMIN.getScene()).setParentMenuId(888L)
                .setTableName("infra_demo11_student").setTableComment("学生表")
                .setModuleName("infra").setBusinessName("demo11").setClassName("InfraDemo11Student")
                .setClassComment("学生").setAuthor("芋道源码")
                .setTemplateType(CodegenTemplateTypeEnum.MASTER_NORMAL.getType())
                .setFrontType(CodegenFrontTypeEnum.VUE3.getType());
        CodegenColumnDO idColumn = new CodegenColumnDO().setColumnName("id").setDataType(JdbcType.BIGINT.name())
                .setId(100L)
                .setColumnComment("编号").setNullable(false).setPrimaryKey(true).setAutoIncrement(true)
                .setJavaType("Long").setJavaField("id").setExample("1024")
                .setCreateOperation(false).setUpdateOperation(true)
                .setListOperation(false)
                .setListOperationResult(true);
        CodegenColumnDO nameColumn = new CodegenColumnDO().setColumnName("name").setDataType(JdbcType.VARCHAR.name())
                .setColumnComment("名字").setNullable(false)
                .setJavaType("String").setJavaField("name").setExample("芋头")
                .setCreateOperation(true).setUpdateOperation(true)
                .setListOperation(true).setListOperationCondition(CodegenColumnListConditionEnum.LIKE.getCondition())
                .setListOperationResult(true)
                .setHtmlType(CodegenColumnHtmlTypeEnum.INPUT.getType());
        CodegenColumnDO descriptionColumn = new CodegenColumnDO().setColumnName("description").setDataType(JdbcType.VARCHAR.name())
                .setColumnComment("简介").setNullable(false)
                .setJavaType("String").setJavaField("description").setExample("我是介绍")
                .setCreateOperation(true).setUpdateOperation(true)
                .setListOperation(false)
                .setListOperationResult(true)
                .setHtmlType(CodegenColumnHtmlTypeEnum.TEXTAREA.getType());
        CodegenColumnDO birthdayColumn = new CodegenColumnDO().setColumnName("birthday").setDataType(JdbcType.DATE.name())
                .setColumnComment("出生日期").setNullable(false)
                .setJavaType("LocalDateTime").setJavaField("birthday")
                .setCreateOperation(true).setUpdateOperation(true)
                .setListOperation(true).setListOperationCondition(CodegenColumnListConditionEnum.EQ.getCondition())
                .setListOperationResult(true)
                .setHtmlType(CodegenColumnHtmlTypeEnum.DATETIME.getType());
        CodegenColumnDO sexColumn = new CodegenColumnDO().setColumnName("sex").setDataType(JdbcType.INTEGER.name())
                .setColumnComment("性别").setNullable(false)
                .setJavaType("Integer").setJavaField("sex").setExample("1")
                .setCreateOperation(true).setUpdateOperation(true)
                .setListOperation(true).setListOperationCondition(CodegenColumnListConditionEnum.EQ.getCondition())
                .setListOperationResult(true)
                .setHtmlType(CodegenColumnHtmlTypeEnum.SELECT.getType())
                .setDictType("system_user_sex");
        CodegenColumnDO enabledColumn = new CodegenColumnDO().setColumnName("enabled").setDataType(JdbcType.BOOLEAN.name())
                .setColumnComment("是否有效").setNullable(false)
                .setJavaType("Boolean").setJavaField("enabled").setExample("true")
                .setCreateOperation(true).setUpdateOperation(true)
                .setListOperation(true).setListOperationCondition(CodegenColumnListConditionEnum.EQ.getCondition())
                .setListOperationResult(true)
                .setHtmlType(CodegenColumnHtmlTypeEnum.RADIO.getType())
                .setDictType("infra_boolean_string");
        CodegenColumnDO avatarColumn = new CodegenColumnDO().setColumnName("avatar").setDataType(JdbcType.VARCHAR.name())
                .setColumnComment("头像").setNullable(false)
                .setJavaType("String").setJavaField("avatar").setExample("https://www.iocoder.cn/1.png")
                .setCreateOperation(true).setUpdateOperation(true)
                .setListOperation(false)
                .setListOperationResult(true)
                .setHtmlType(CodegenColumnHtmlTypeEnum.IMAGE_UPLOAD.getType());
        CodegenColumnDO videoColumn = new CodegenColumnDO().setColumnName("video").setDataType(JdbcType.VARCHAR.name())
                .setColumnComment("附件").setNullable(true)
                .setJavaType("String").setJavaField("video").setExample("https://www.iocoder.cn/1.mp4")
                .setCreateOperation(true).setUpdateOperation(true)
                .setListOperation(false)
                .setListOperationResult(true)
                .setHtmlType(CodegenColumnHtmlTypeEnum.FILE_UPLOAD.getType());
        CodegenColumnDO memoColumn = new CodegenColumnDO().setColumnName("memo").setDataType(JdbcType.VARCHAR.name())
                .setColumnComment("备注").setNullable(false)
                .setJavaType("String").setJavaField("memo").setExample("我是备注")
                .setCreateOperation(true).setUpdateOperation(true)
                .setListOperation(false)
                .setListOperationResult(true)
                .setHtmlType(CodegenColumnHtmlTypeEnum.EDITOR.getType());
        CodegenColumnDO createTimeColumn = new CodegenColumnDO().setColumnName("create_time").setDataType(JdbcType.DATE.name())
                .setColumnComment("创建时间").setNullable(true)
                .setJavaType("LocalDateTime").setJavaField("createTime")
                .setCreateOperation(false).setUpdateOperation(false)
                .setListOperation(true).setListOperationCondition(CodegenColumnListConditionEnum.BETWEEN.getCondition())
                .setListOperationResult(true)
                .setHtmlType(CodegenColumnHtmlTypeEnum.DATETIME.getType());
        List<CodegenColumnDO> columns = Arrays.asList(idColumn, nameColumn, descriptionColumn, birthdayColumn,
                sexColumn, enabledColumn, avatarColumn, videoColumn, memoColumn, createTimeColumn);

        // 子表（联系人）
        CodegenTableDO contactTable = new CodegenTableDO().setScene(CodegenSceneEnum.ADMIN.getScene())
                .setTableName("infra_demo11_student_contact").setTableComment("学生联系人表")
                .setModuleName("infra").setBusinessName("demo11").setClassName("InfraDemo11StudentContact")
                .setClassComment("学生联系人").setAuthor("芋道源码")
                .setTemplateType(CodegenTemplateTypeEnum.SUB.getType())
                .setFrontType(CodegenFrontTypeEnum.VUE3.getType())
                .setSubJoinColumnId(100L).setSubJoinMany(true);
        CodegenColumnDO contactIdColumn = new CodegenColumnDO().setColumnName("id").setDataType(JdbcType.BIGINT.name())
                .setColumnComment("编号").setNullable(false).setPrimaryKey(true).setAutoIncrement(true)
                .setJavaType("Long").setJavaField("id").setExample("1024")
                .setCreateOperation(false).setUpdateOperation(true)
                .setListOperation(false)
                .setListOperationResult(true);
        CodegenColumnDO contactStudentIdColumn = new CodegenColumnDO().setColumnName("student_id").setDataType(JdbcType.BIGINT.name())
                .setColumnComment("学生编号").setNullable(false).setPrimaryKey(false)
                .setJavaType("Long").setJavaField("studentId").setExample("2048")
                .setCreateOperation(true).setUpdateOperation(true)
                .setListOperation(false)
                .setListOperationResult(true)
                .setId(100L);
        CodegenColumnDO contactNameColumn = new CodegenColumnDO().setColumnName("name").setDataType(JdbcType.VARCHAR.name())
                .setColumnComment("名字").setNullable(false)
                .setJavaType("String").setJavaField("name").setExample("芋头")
                .setCreateOperation(true).setUpdateOperation(true)
                .setListOperation(true).setListOperationCondition(CodegenColumnListConditionEnum.LIKE.getCondition())
                .setListOperationResult(true)
                .setHtmlType(CodegenColumnHtmlTypeEnum.INPUT.getType());
        CodegenColumnDO contactDescriptionColumn = new CodegenColumnDO().setColumnName("description").setDataType(JdbcType.VARCHAR.name())
                .setColumnComment("简介").setNullable(false)
                .setJavaType("String").setJavaField("description").setExample("我是介绍")
                .setCreateOperation(true).setUpdateOperation(true)
                .setListOperation(false)
                .setListOperationResult(true)
                .setHtmlType(CodegenColumnHtmlTypeEnum.TEXTAREA.getType());
        CodegenColumnDO contactBirthdayColumn = new CodegenColumnDO().setColumnName("birthday").setDataType(JdbcType.DATE.name())
                .setColumnComment("出生日期").setNullable(false)
                .setJavaType("LocalDateTime").setJavaField("birthday")
                .setCreateOperation(true).setUpdateOperation(true)
                .setListOperation(true).setListOperationCondition(CodegenColumnListConditionEnum.EQ.getCondition())
                .setListOperationResult(true)
                .setHtmlType(CodegenColumnHtmlTypeEnum.DATETIME.getType());
        CodegenColumnDO contactSexColumn = new CodegenColumnDO().setColumnName("sex").setDataType(JdbcType.INTEGER.name())
                .setColumnComment("性别").setNullable(false)
                .setJavaType("Integer").setJavaField("sex").setExample("1")
                .setCreateOperation(true).setUpdateOperation(true)
                .setListOperation(true).setListOperationCondition(CodegenColumnListConditionEnum.EQ.getCondition())
                .setListOperationResult(true)
                .setHtmlType(CodegenColumnHtmlTypeEnum.SELECT.getType())
                .setDictType("system_user_sex");
        CodegenColumnDO contactEnabledColumn = new CodegenColumnDO().setColumnName("enabled").setDataType(JdbcType.BOOLEAN.name())
                .setColumnComment("是否有效").setNullable(false)
                .setJavaType("Boolean").setJavaField("enabled").setExample("true")
                .setCreateOperation(true).setUpdateOperation(true)
                .setListOperation(true).setListOperationCondition(CodegenColumnListConditionEnum.EQ.getCondition())
                .setListOperationResult(true)
                .setHtmlType(CodegenColumnHtmlTypeEnum.RADIO.getType())
                .setDictType("infra_boolean_string");
        CodegenColumnDO contactAvatarColumn = new CodegenColumnDO().setColumnName("avatar").setDataType(JdbcType.VARCHAR.name())
                .setColumnComment("头像").setNullable(false)
                .setJavaType("String").setJavaField("avatar").setExample("https://www.iocoder.cn/1.png")
                .setCreateOperation(true).setUpdateOperation(true)
                .setListOperation(false)
                .setListOperationResult(true)
                .setHtmlType(CodegenColumnHtmlTypeEnum.IMAGE_UPLOAD.getType());
        CodegenColumnDO contactVideoColumn = new CodegenColumnDO().setColumnName("video").setDataType(JdbcType.VARCHAR.name())
                .setColumnComment("附件").setNullable(true)
                .setJavaType("String").setJavaField("video").setExample("https://www.iocoder.cn/1.mp4")
                .setCreateOperation(true).setUpdateOperation(true)
                .setListOperation(false)
                .setListOperationResult(true)
                .setHtmlType(CodegenColumnHtmlTypeEnum.FILE_UPLOAD.getType());
        CodegenColumnDO contactMemoColumn = new CodegenColumnDO().setColumnName("memo").setDataType(JdbcType.VARCHAR.name())
                .setColumnComment("备注").setNullable(false)
                .setJavaType("String").setJavaField("memo").setExample("我是备注")
                .setCreateOperation(true).setUpdateOperation(true)
                .setListOperation(false)
                .setListOperationResult(true)
                .setHtmlType(CodegenColumnHtmlTypeEnum.EDITOR.getType());
        CodegenColumnDO contactCreateTimeColumn = new CodegenColumnDO().setColumnName("create_time").setDataType(JdbcType.DATE.name())
                .setColumnComment("创建时间").setNullable(true)
                .setJavaType("LocalDateTime").setJavaField("createTime")
                .setCreateOperation(false).setUpdateOperation(false)
                .setListOperation(true).setListOperationCondition(CodegenColumnListConditionEnum.BETWEEN.getCondition())
                .setListOperationResult(true)
                .setHtmlType(CodegenColumnHtmlTypeEnum.DATETIME.getType());
        List<CodegenColumnDO> contactColumns = Arrays.asList(contactIdColumn, contactStudentIdColumn,
                contactNameColumn, contactDescriptionColumn, contactBirthdayColumn,
                contactSexColumn, contactEnabledColumn, contactAvatarColumn, contactVideoColumn, contactMemoColumn, contactCreateTimeColumn);

        // 子表（班主任）
        CodegenTableDO teacherTable = new CodegenTableDO().setScene(CodegenSceneEnum.ADMIN.getScene())
                .setTableName("infra_demo11_student_teacher").setTableComment("学生班主任表")
                .setModuleName("infra").setBusinessName("demo11").setClassName("InfraDemo11StudentTeacher")
                .setClassComment("学生班主任").setAuthor("芋道源码")
                .setTemplateType(CodegenTemplateTypeEnum.SUB.getType())
                .setFrontType(CodegenFrontTypeEnum.VUE3.getType())
                .setSubJoinColumnId(200L).setSubJoinMany(false);
        CodegenColumnDO teacherIdColumn = new CodegenColumnDO().setColumnName("id").setDataType(JdbcType.BIGINT.name())
                .setColumnComment("编号").setNullable(false).setPrimaryKey(true).setAutoIncrement(true)
                .setJavaType("Long").setJavaField("id").setExample("1024")
                .setCreateOperation(false).setUpdateOperation(true)
                .setListOperation(false)
                .setListOperationResult(true);
        CodegenColumnDO teacherStudentIdColumn = new CodegenColumnDO().setColumnName("student_id").setDataType(JdbcType.BIGINT.name())
                .setColumnComment("学生编号").setNullable(false).setPrimaryKey(false)
                .setJavaType("Long").setJavaField("studentId").setExample("2048")
                .setCreateOperation(true).setUpdateOperation(true)
                .setListOperation(false)
                .setListOperationResult(true)
                .setId(200L);
        CodegenColumnDO teacherNameColumn = new CodegenColumnDO().setColumnName("name").setDataType(JdbcType.VARCHAR.name())
                .setColumnComment("名字").setNullable(false)
                .setJavaType("String").setJavaField("name").setExample("芋头")
                .setCreateOperation(true).setUpdateOperation(true)
                .setListOperation(true).setListOperationCondition(CodegenColumnListConditionEnum.LIKE.getCondition())
                .setListOperationResult(true)
                .setHtmlType(CodegenColumnHtmlTypeEnum.INPUT.getType());
        CodegenColumnDO teacherDescriptionColumn = new CodegenColumnDO().setColumnName("description").setDataType(JdbcType.VARCHAR.name())
                .setColumnComment("简介").setNullable(false)
                .setJavaType("String").setJavaField("description").setExample("我是介绍")
                .setCreateOperation(true).setUpdateOperation(true)
                .setListOperation(false)
                .setListOperationResult(true)
                .setHtmlType(CodegenColumnHtmlTypeEnum.TEXTAREA.getType());
        CodegenColumnDO teacherBirthdayColumn = new CodegenColumnDO().setColumnName("birthday").setDataType(JdbcType.DATE.name())
                .setColumnComment("出生日期").setNullable(false)
                .setJavaType("LocalDateTime").setJavaField("birthday")
                .setCreateOperation(true).setUpdateOperation(true)
                .setListOperation(true).setListOperationCondition(CodegenColumnListConditionEnum.EQ.getCondition())
                .setListOperationResult(true)
                .setHtmlType(CodegenColumnHtmlTypeEnum.DATETIME.getType());
        CodegenColumnDO teacherSexColumn = new CodegenColumnDO().setColumnName("sex").setDataType(JdbcType.INTEGER.name())
                .setColumnComment("性别").setNullable(false)
                .setJavaType("Integer").setJavaField("sex").setExample("1")
                .setCreateOperation(true).setUpdateOperation(true)
                .setListOperation(true).setListOperationCondition(CodegenColumnListConditionEnum.EQ.getCondition())
                .setListOperationResult(true)
                .setHtmlType(CodegenColumnHtmlTypeEnum.SELECT.getType())
                .setDictType("system_user_sex");
        CodegenColumnDO teacherEnabledColumn = new CodegenColumnDO().setColumnName("enabled").setDataType(JdbcType.BOOLEAN.name())
                .setColumnComment("是否有效").setNullable(false)
                .setJavaType("Boolean").setJavaField("enabled").setExample("true")
                .setCreateOperation(true).setUpdateOperation(true)
                .setListOperation(true).setListOperationCondition(CodegenColumnListConditionEnum.EQ.getCondition())
                .setListOperationResult(true)
                .setHtmlType(CodegenColumnHtmlTypeEnum.RADIO.getType())
                .setDictType("infra_boolean_string");
        CodegenColumnDO teacherAvatarColumn = new CodegenColumnDO().setColumnName("avatar").setDataType(JdbcType.VARCHAR.name())
                .setColumnComment("头像").setNullable(false)
                .setJavaType("String").setJavaField("avatar").setExample("https://www.iocoder.cn/1.png")
                .setCreateOperation(true).setUpdateOperation(true)
                .setListOperation(false)
                .setListOperationResult(true)
                .setHtmlType(CodegenColumnHtmlTypeEnum.IMAGE_UPLOAD.getType());
        CodegenColumnDO teacherVideoColumn = new CodegenColumnDO().setColumnName("video").setDataType(JdbcType.VARCHAR.name())
                .setColumnComment("附件").setNullable(true)
                .setJavaType("String").setJavaField("video").setExample("https://www.iocoder.cn/1.mp4")
                .setCreateOperation(true).setUpdateOperation(true)
                .setListOperation(false)
                .setListOperationResult(true)
                .setHtmlType(CodegenColumnHtmlTypeEnum.FILE_UPLOAD.getType());
        CodegenColumnDO teacherMemoColumn = new CodegenColumnDO().setColumnName("memo").setDataType(JdbcType.VARCHAR.name())
                .setColumnComment("备注").setNullable(false)
                .setJavaType("String").setJavaField("memo").setExample("我是备注")
                .setCreateOperation(true).setUpdateOperation(true)
                .setListOperation(false)
                .setListOperationResult(true)
                .setHtmlType(CodegenColumnHtmlTypeEnum.EDITOR.getType());
        CodegenColumnDO teacherCreateTimeColumn = new CodegenColumnDO().setColumnName("create_time").setDataType(JdbcType.DATE.name())
                .setColumnComment("创建时间").setNullable(true)
                .setJavaType("LocalDateTime").setJavaField("createTime")
                .setCreateOperation(false).setUpdateOperation(false)
                .setListOperation(true).setListOperationCondition(CodegenColumnListConditionEnum.BETWEEN.getCondition())
                .setListOperationResult(true)
                .setHtmlType(CodegenColumnHtmlTypeEnum.DATETIME.getType());
        List<CodegenColumnDO> teacherColumns = Arrays.asList(teacherIdColumn, teacherStudentIdColumn,
                teacherNameColumn, teacherDescriptionColumn, teacherBirthdayColumn,
                teacherSexColumn, teacherEnabledColumn, teacherAvatarColumn, teacherVideoColumn, teacherMemoColumn, teacherCreateTimeColumn);

        // 调用
        Map<String, String> result = codegenEngine.execute(table, columns,
                Arrays.asList(contactTable, teacherTable), Arrays.asList(contactColumns, teacherColumns));

        // 构建 zip 包
        writeFile(result, "/Users/yunai/test/demo11.zip");

        // 断言
        assertEquals(27, result.size());

        for (Map.Entry<String, String> entry : result.entrySet()) {
            System.out.println(entry.getKey());
            System.out.println(entry.getValue());
            System.out.println("");
            System.out.println("");
            System.out.println("");
        }
    }

    @Test
    public void testExecute_vue3_masterInner() {
        // 准备请求参数
        // 主表
        CodegenTableDO table = new CodegenTableDO().setScene(CodegenSceneEnum.ADMIN.getScene()).setParentMenuId(888L)
                .setTableName("infra_demo12_student").setTableComment("学生表")
                .setModuleName("infra").setBusinessName("demo12").setClassName("InfraDemo12Student")
                .setClassComment("学生").setAuthor("芋道源码")
                .setTemplateType(CodegenTemplateTypeEnum.MASTER_INNER.getType())
                .setFrontType(CodegenFrontTypeEnum.VUE3.getType());
        CodegenColumnDO idColumn = new CodegenColumnDO().setColumnName("id").setDataType(JdbcType.BIGINT.name())
                .setId(100L)
                .setColumnComment("编号").setNullable(false).setPrimaryKey(true).setAutoIncrement(true)
                .setJavaType("Long").setJavaField("id").setExample("1024")
                .setCreateOperation(false).setUpdateOperation(true)
                .setListOperation(false)
                .setListOperationResult(true);
        CodegenColumnDO nameColumn = new CodegenColumnDO().setColumnName("name").setDataType(JdbcType.VARCHAR.name())
                .setColumnComment("名字").setNullable(false)
                .setJavaType("String").setJavaField("name").setExample("芋头")
                .setCreateOperation(true).setUpdateOperation(true)
                .setListOperation(true).setListOperationCondition(CodegenColumnListConditionEnum.LIKE.getCondition())
                .setListOperationResult(true)
                .setHtmlType(CodegenColumnHtmlTypeEnum.INPUT.getType());
        CodegenColumnDO descriptionColumn = new CodegenColumnDO().setColumnName("description").setDataType(JdbcType.VARCHAR.name())
                .setColumnComment("简介").setNullable(false)
                .setJavaType("String").setJavaField("description").setExample("我是介绍")
                .setCreateOperation(true).setUpdateOperation(true)
                .setListOperation(false)
                .setListOperationResult(true)
                .setHtmlType(CodegenColumnHtmlTypeEnum.TEXTAREA.getType());
        CodegenColumnDO birthdayColumn = new CodegenColumnDO().setColumnName("birthday").setDataType(JdbcType.DATE.name())
                .setColumnComment("出生日期").setNullable(false)
                .setJavaType("LocalDateTime").setJavaField("birthday")
                .setCreateOperation(true).setUpdateOperation(true)
                .setListOperation(true).setListOperationCondition(CodegenColumnListConditionEnum.EQ.getCondition())
                .setListOperationResult(true)
                .setHtmlType(CodegenColumnHtmlTypeEnum.DATETIME.getType());
        CodegenColumnDO sexColumn = new CodegenColumnDO().setColumnName("sex").setDataType(JdbcType.INTEGER.name())
                .setColumnComment("性别").setNullable(false)
                .setJavaType("Integer").setJavaField("sex").setExample("1")
                .setCreateOperation(true).setUpdateOperation(true)
                .setListOperation(true).setListOperationCondition(CodegenColumnListConditionEnum.EQ.getCondition())
                .setListOperationResult(true)
                .setHtmlType(CodegenColumnHtmlTypeEnum.SELECT.getType())
                .setDictType("system_user_sex");
        CodegenColumnDO enabledColumn = new CodegenColumnDO().setColumnName("enabled").setDataType(JdbcType.BOOLEAN.name())
                .setColumnComment("是否有效").setNullable(false)
                .setJavaType("Boolean").setJavaField("enabled").setExample("true")
                .setCreateOperation(true).setUpdateOperation(true)
                .setListOperation(true).setListOperationCondition(CodegenColumnListConditionEnum.EQ.getCondition())
                .setListOperationResult(true)
                .setHtmlType(CodegenColumnHtmlTypeEnum.RADIO.getType())
                .setDictType("infra_boolean_string");
        CodegenColumnDO avatarColumn = new CodegenColumnDO().setColumnName("avatar").setDataType(JdbcType.VARCHAR.name())
                .setColumnComment("头像").setNullable(false)
                .setJavaType("String").setJavaField("avatar").setExample("https://www.iocoder.cn/1.png")
                .setCreateOperation(true).setUpdateOperation(true)
                .setListOperation(false)
                .setListOperationResult(true)
                .setHtmlType(CodegenColumnHtmlTypeEnum.IMAGE_UPLOAD.getType());
        CodegenColumnDO videoColumn = new CodegenColumnDO().setColumnName("video").setDataType(JdbcType.VARCHAR.name())
                .setColumnComment("附件").setNullable(true)
                .setJavaType("String").setJavaField("video").setExample("https://www.iocoder.cn/1.mp4")
                .setCreateOperation(true).setUpdateOperation(true)
                .setListOperation(false)
                .setListOperationResult(true)
                .setHtmlType(CodegenColumnHtmlTypeEnum.FILE_UPLOAD.getType());
        CodegenColumnDO memoColumn = new CodegenColumnDO().setColumnName("memo").setDataType(JdbcType.VARCHAR.name())
                .setColumnComment("备注").setNullable(false)
                .setJavaType("String").setJavaField("memo").setExample("我是备注")
                .setCreateOperation(true).setUpdateOperation(true)
                .setListOperation(false)
                .setListOperationResult(true)
                .setHtmlType(CodegenColumnHtmlTypeEnum.EDITOR.getType());
        CodegenColumnDO createTimeColumn = new CodegenColumnDO().setColumnName("create_time").setDataType(JdbcType.DATE.name())
                .setColumnComment("创建时间").setNullable(true)
                .setJavaType("LocalDateTime").setJavaField("createTime")
                .setCreateOperation(false).setUpdateOperation(false)
                .setListOperation(true).setListOperationCondition(CodegenColumnListConditionEnum.BETWEEN.getCondition())
                .setListOperationResult(true)
                .setHtmlType(CodegenColumnHtmlTypeEnum.DATETIME.getType());
        List<CodegenColumnDO> columns = Arrays.asList(idColumn, nameColumn, descriptionColumn, birthdayColumn,
                sexColumn, enabledColumn, avatarColumn, videoColumn, memoColumn, createTimeColumn);

        // 子表（联系人）
        CodegenTableDO contactTable = new CodegenTableDO().setScene(CodegenSceneEnum.ADMIN.getScene())
                .setTableName("infra_demo12_student_contact").setTableComment("学生联系人表")
                .setModuleName("infra").setBusinessName("demo12").setClassName("InfraDemo12StudentContact")
                .setClassComment("学生联系人").setAuthor("芋道源码")
                .setTemplateType(CodegenTemplateTypeEnum.SUB.getType())
                .setFrontType(CodegenFrontTypeEnum.VUE3.getType())
                .setSubJoinColumnId(100L).setSubJoinMany(true);
        CodegenColumnDO contactIdColumn = new CodegenColumnDO().setColumnName("id").setDataType(JdbcType.BIGINT.name())
                .setColumnComment("编号").setNullable(false).setPrimaryKey(true).setAutoIncrement(true)
                .setJavaType("Long").setJavaField("id").setExample("1024")
                .setCreateOperation(false).setUpdateOperation(true)
                .setListOperation(false)
                .setListOperationResult(true);
        CodegenColumnDO contactStudentIdColumn = new CodegenColumnDO().setColumnName("student_id").setDataType(JdbcType.BIGINT.name())
                .setColumnComment("学生编号").setNullable(false).setPrimaryKey(false)
                .setJavaType("Long").setJavaField("studentId").setExample("2048")
                .setCreateOperation(true).setUpdateOperation(true)
                .setListOperation(false)
                .setListOperationResult(true)
                .setId(100L);
        CodegenColumnDO contactNameColumn = new CodegenColumnDO().setColumnName("name").setDataType(JdbcType.VARCHAR.name())
                .setColumnComment("名字").setNullable(false)
                .setJavaType("String").setJavaField("name").setExample("芋头")
                .setCreateOperation(true).setUpdateOperation(true)
                .setListOperation(true).setListOperationCondition(CodegenColumnListConditionEnum.LIKE.getCondition())
                .setListOperationResult(true)
                .setHtmlType(CodegenColumnHtmlTypeEnum.INPUT.getType());
        CodegenColumnDO contactDescriptionColumn = new CodegenColumnDO().setColumnName("description").setDataType(JdbcType.VARCHAR.name())
                .setColumnComment("简介").setNullable(false)
                .setJavaType("String").setJavaField("description").setExample("我是介绍")
                .setCreateOperation(true).setUpdateOperation(true)
                .setListOperation(false)
                .setListOperationResult(true)
                .setHtmlType(CodegenColumnHtmlTypeEnum.TEXTAREA.getType());
        CodegenColumnDO contactBirthdayColumn = new CodegenColumnDO().setColumnName("birthday").setDataType(JdbcType.DATE.name())
                .setColumnComment("出生日期").setNullable(false)
                .setJavaType("LocalDateTime").setJavaField("birthday")
                .setCreateOperation(true).setUpdateOperation(true)
                .setListOperation(true).setListOperationCondition(CodegenColumnListConditionEnum.EQ.getCondition())
                .setListOperationResult(true)
                .setHtmlType(CodegenColumnHtmlTypeEnum.DATETIME.getType());
        CodegenColumnDO contactSexColumn = new CodegenColumnDO().setColumnName("sex").setDataType(JdbcType.INTEGER.name())
                .setColumnComment("性别").setNullable(false)
                .setJavaType("Integer").setJavaField("sex").setExample("1")
                .setCreateOperation(true).setUpdateOperation(true)
                .setListOperation(true).setListOperationCondition(CodegenColumnListConditionEnum.EQ.getCondition())
                .setListOperationResult(true)
                .setHtmlType(CodegenColumnHtmlTypeEnum.SELECT.getType())
                .setDictType("system_user_sex");
        CodegenColumnDO contactEnabledColumn = new CodegenColumnDO().setColumnName("enabled").setDataType(JdbcType.BOOLEAN.name())
                .setColumnComment("是否有效").setNullable(false)
                .setJavaType("Boolean").setJavaField("enabled").setExample("true")
                .setCreateOperation(true).setUpdateOperation(true)
                .setListOperation(true).setListOperationCondition(CodegenColumnListConditionEnum.EQ.getCondition())
                .setListOperationResult(true)
                .setHtmlType(CodegenColumnHtmlTypeEnum.RADIO.getType())
                .setDictType("infra_boolean_string");
        CodegenColumnDO contactAvatarColumn = new CodegenColumnDO().setColumnName("avatar").setDataType(JdbcType.VARCHAR.name())
                .setColumnComment("头像").setNullable(false)
                .setJavaType("String").setJavaField("avatar").setExample("https://www.iocoder.cn/1.png")
                .setCreateOperation(true).setUpdateOperation(true)
                .setListOperation(false)
                .setListOperationResult(true)
                .setHtmlType(CodegenColumnHtmlTypeEnum.IMAGE_UPLOAD.getType());
        CodegenColumnDO contactVideoColumn = new CodegenColumnDO().setColumnName("video").setDataType(JdbcType.VARCHAR.name())
                .setColumnComment("附件").setNullable(true)
                .setJavaType("String").setJavaField("video").setExample("https://www.iocoder.cn/1.mp4")
                .setCreateOperation(true).setUpdateOperation(true)
                .setListOperation(false)
                .setListOperationResult(true)
                .setHtmlType(CodegenColumnHtmlTypeEnum.FILE_UPLOAD.getType());
        CodegenColumnDO contactMemoColumn = new CodegenColumnDO().setColumnName("memo").setDataType(JdbcType.VARCHAR.name())
                .setColumnComment("备注").setNullable(false)
                .setJavaType("String").setJavaField("memo").setExample("我是备注")
                .setCreateOperation(true).setUpdateOperation(true)
                .setListOperation(false)
                .setListOperationResult(true)
                .setHtmlType(CodegenColumnHtmlTypeEnum.EDITOR.getType());
        CodegenColumnDO contactCreateTimeColumn = new CodegenColumnDO().setColumnName("create_time").setDataType(JdbcType.DATE.name())
                .setColumnComment("创建时间").setNullable(true)
                .setJavaType("LocalDateTime").setJavaField("createTime")
                .setCreateOperation(false).setUpdateOperation(false)
                .setListOperation(true).setListOperationCondition(CodegenColumnListConditionEnum.BETWEEN.getCondition())
                .setListOperationResult(true)
                .setHtmlType(CodegenColumnHtmlTypeEnum.DATETIME.getType());
        List<CodegenColumnDO> contactColumns = Arrays.asList(contactIdColumn, contactStudentIdColumn,
                contactNameColumn, contactDescriptionColumn, contactBirthdayColumn,
                contactSexColumn, contactEnabledColumn, contactAvatarColumn, contactVideoColumn, contactMemoColumn, contactCreateTimeColumn);

        // 子表（班主任）
        CodegenTableDO teacherTable = new CodegenTableDO().setScene(CodegenSceneEnum.ADMIN.getScene())
                .setTableName("infra_demo12_student_teacher").setTableComment("学生班主任表")
                .setModuleName("infra").setBusinessName("demo12").setClassName("InfraDemo12StudentTeacher")
                .setClassComment("学生班主任").setAuthor("芋道源码")
                .setTemplateType(CodegenTemplateTypeEnum.SUB.getType())
                .setFrontType(CodegenFrontTypeEnum.VUE3.getType())
                .setSubJoinColumnId(200L).setSubJoinMany(false);
        CodegenColumnDO teacherIdColumn = new CodegenColumnDO().setColumnName("id").setDataType(JdbcType.BIGINT.name())
                .setColumnComment("编号").setNullable(false).setPrimaryKey(true).setAutoIncrement(true)
                .setJavaType("Long").setJavaField("id").setExample("1024")
                .setCreateOperation(false).setUpdateOperation(true)
                .setListOperation(false)
                .setListOperationResult(true);
        CodegenColumnDO teacherStudentIdColumn = new CodegenColumnDO().setColumnName("student_id").setDataType(JdbcType.BIGINT.name())
                .setColumnComment("学生编号").setNullable(false).setPrimaryKey(false)
                .setJavaType("Long").setJavaField("studentId").setExample("2048")
                .setCreateOperation(true).setUpdateOperation(true)
                .setListOperation(false)
                .setListOperationResult(true)
                .setId(200L);
        CodegenColumnDO teacherNameColumn = new CodegenColumnDO().setColumnName("name").setDataType(JdbcType.VARCHAR.name())
                .setColumnComment("名字").setNullable(false)
                .setJavaType("String").setJavaField("name").setExample("芋头")
                .setCreateOperation(true).setUpdateOperation(true)
                .setListOperation(true).setListOperationCondition(CodegenColumnListConditionEnum.LIKE.getCondition())
                .setListOperationResult(true)
                .setHtmlType(CodegenColumnHtmlTypeEnum.INPUT.getType());
        CodegenColumnDO teacherDescriptionColumn = new CodegenColumnDO().setColumnName("description").setDataType(JdbcType.VARCHAR.name())
                .setColumnComment("简介").setNullable(false)
                .setJavaType("String").setJavaField("description").setExample("我是介绍")
                .setCreateOperation(true).setUpdateOperation(true)
                .setListOperation(false)
                .setListOperationResult(true)
                .setHtmlType(CodegenColumnHtmlTypeEnum.TEXTAREA.getType());
        CodegenColumnDO teacherBirthdayColumn = new CodegenColumnDO().setColumnName("birthday").setDataType(JdbcType.DATE.name())
                .setColumnComment("出生日期").setNullable(false)
                .setJavaType("LocalDateTime").setJavaField("birthday")
                .setCreateOperation(true).setUpdateOperation(true)
                .setListOperation(true).setListOperationCondition(CodegenColumnListConditionEnum.EQ.getCondition())
                .setListOperationResult(true)
                .setHtmlType(CodegenColumnHtmlTypeEnum.DATETIME.getType());
        CodegenColumnDO teacherSexColumn = new CodegenColumnDO().setColumnName("sex").setDataType(JdbcType.INTEGER.name())
                .setColumnComment("性别").setNullable(false)
                .setJavaType("Integer").setJavaField("sex").setExample("1")
                .setCreateOperation(true).setUpdateOperation(true)
                .setListOperation(true).setListOperationCondition(CodegenColumnListConditionEnum.EQ.getCondition())
                .setListOperationResult(true)
                .setHtmlType(CodegenColumnHtmlTypeEnum.SELECT.getType())
                .setDictType("system_user_sex");
        CodegenColumnDO teacherEnabledColumn = new CodegenColumnDO().setColumnName("enabled").setDataType(JdbcType.BOOLEAN.name())
                .setColumnComment("是否有效").setNullable(false)
                .setJavaType("Boolean").setJavaField("enabled").setExample("true")
                .setCreateOperation(true).setUpdateOperation(true)
                .setListOperation(true).setListOperationCondition(CodegenColumnListConditionEnum.EQ.getCondition())
                .setListOperationResult(true)
                .setHtmlType(CodegenColumnHtmlTypeEnum.RADIO.getType())
                .setDictType("infra_boolean_string");
        CodegenColumnDO teacherAvatarColumn = new CodegenColumnDO().setColumnName("avatar").setDataType(JdbcType.VARCHAR.name())
                .setColumnComment("头像").setNullable(false)
                .setJavaType("String").setJavaField("avatar").setExample("https://www.iocoder.cn/1.png")
                .setCreateOperation(true).setUpdateOperation(true)
                .setListOperation(false)
                .setListOperationResult(true)
                .setHtmlType(CodegenColumnHtmlTypeEnum.IMAGE_UPLOAD.getType());
        CodegenColumnDO teacherVideoColumn = new CodegenColumnDO().setColumnName("video").setDataType(JdbcType.VARCHAR.name())
                .setColumnComment("附件").setNullable(true)
                .setJavaType("String").setJavaField("video").setExample("https://www.iocoder.cn/1.mp4")
                .setCreateOperation(true).setUpdateOperation(true)
                .setListOperation(false)
                .setListOperationResult(true)
                .setHtmlType(CodegenColumnHtmlTypeEnum.FILE_UPLOAD.getType());
        CodegenColumnDO teacherMemoColumn = new CodegenColumnDO().setColumnName("memo").setDataType(JdbcType.VARCHAR.name())
                .setColumnComment("备注").setNullable(false)
                .setJavaType("String").setJavaField("memo").setExample("我是备注")
                .setCreateOperation(true).setUpdateOperation(true)
                .setListOperation(false)
                .setListOperationResult(true)
                .setHtmlType(CodegenColumnHtmlTypeEnum.EDITOR.getType());
        CodegenColumnDO teacherCreateTimeColumn = new CodegenColumnDO().setColumnName("create_time").setDataType(JdbcType.DATE.name())
                .setColumnComment("创建时间").setNullable(true)
                .setJavaType("LocalDateTime").setJavaField("createTime")
                .setCreateOperation(false).setUpdateOperation(false)
                .setListOperation(true).setListOperationCondition(CodegenColumnListConditionEnum.BETWEEN.getCondition())
                .setListOperationResult(true)
                .setHtmlType(CodegenColumnHtmlTypeEnum.DATETIME.getType());
        List<CodegenColumnDO> teacherColumns = Arrays.asList(teacherIdColumn, teacherStudentIdColumn,
                teacherNameColumn, teacherDescriptionColumn, teacherBirthdayColumn,
                teacherSexColumn, teacherEnabledColumn, teacherAvatarColumn, teacherVideoColumn, teacherMemoColumn, teacherCreateTimeColumn);

        // 调用
        Map<String, String> result = codegenEngine.execute(table, columns,
                Arrays.asList(contactTable, teacherTable), Arrays.asList(contactColumns, teacherColumns));

        // 构建 zip 包
        writeFile(result, "/Users/yunai/test/demo12.zip");

        // 断言
        assertEquals(27, result.size());

        for (Map.Entry<String, String> entry : result.entrySet()) {
            System.out.println(entry.getKey());
            System.out.println(entry.getValue());
            System.out.println("");
            System.out.println("");
            System.out.println("");
        }
    }

    private void assertPathContentEquals(String path, Map<String, String> result, String key) {
        String pathContent = ResourceUtil.readUtf8Str("codegen/" + path);
        String valueContent = result.get(key);
        assertEquals(pathContent, valueContent);
    }

    /**
     * 将生成的代码，写入到文件
     *
     * 用途：方便本地调试
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

}
