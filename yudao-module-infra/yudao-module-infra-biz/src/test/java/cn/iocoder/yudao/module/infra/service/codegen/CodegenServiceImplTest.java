package cn.iocoder.yudao.module.infra.service.codegen;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.map.MapUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.infra.controller.admin.codegen.vo.CodegenCreateListReqVO;
import cn.iocoder.yudao.module.infra.controller.admin.codegen.vo.CodegenUpdateReqVO;
import cn.iocoder.yudao.module.infra.controller.admin.codegen.vo.column.CodegenColumnSaveReqVO;
import cn.iocoder.yudao.module.infra.controller.admin.codegen.vo.table.CodegenTablePageReqVO;
import cn.iocoder.yudao.module.infra.controller.admin.codegen.vo.table.DatabaseTableRespVO;
import cn.iocoder.yudao.module.infra.dal.dataobject.codegen.CodegenColumnDO;
import cn.iocoder.yudao.module.infra.dal.dataobject.codegen.CodegenTableDO;
import cn.iocoder.yudao.module.infra.dal.mysql.codegen.CodegenColumnMapper;
import cn.iocoder.yudao.module.infra.dal.mysql.codegen.CodegenTableMapper;
import cn.iocoder.yudao.module.infra.enums.codegen.CodegenFrontTypeEnum;
import cn.iocoder.yudao.module.infra.enums.codegen.CodegenSceneEnum;
import cn.iocoder.yudao.module.infra.enums.codegen.CodegenTemplateTypeEnum;
import cn.iocoder.yudao.module.infra.framework.codegen.config.CodegenProperties;
import cn.iocoder.yudao.module.infra.service.codegen.inner.CodegenBuilder;
import cn.iocoder.yudao.module.infra.service.codegen.inner.CodegenEngine;
import cn.iocoder.yudao.module.infra.service.db.DatabaseTableService;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import com.baomidou.mybatisplus.generator.config.po.TableField;
import com.baomidou.mybatisplus.generator.config.po.TableInfo;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.buildBetweenTime;
import static cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils.buildTime;
import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.cloneIgnoreId;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.*;
import static cn.iocoder.yudao.module.infra.enums.ErrorCodeConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * {@link CodegenServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import(CodegenServiceImpl.class)
public class CodegenServiceImplTest extends BaseDbUnitTest {

    @Resource
    private CodegenServiceImpl codegenService;

    @Resource
    private CodegenTableMapper codegenTableMapper;
    @Resource
    private CodegenColumnMapper codegenColumnMapper;

    @MockBean
    private DatabaseTableService databaseTableService;

    @MockBean
    private AdminUserApi userApi;

    @MockBean
    private CodegenBuilder codegenBuilder;
    @MockBean
    private CodegenEngine codegenEngine;

    @MockBean
    private CodegenProperties codegenProperties;

    @Test
    public void testCreateCodegenList() {
        // 准备参数
        Long userId = randomLongId();
        CodegenCreateListReqVO reqVO = randomPojo(CodegenCreateListReqVO.class,
                o -> o.setDataSourceConfigId(1L).setTableNames(Collections.singletonList("t_yunai")));
        // mock 方法（TableInfo）
        TableInfo tableInfo = mock(TableInfo.class);
        when(databaseTableService.getTable(eq(1L), eq("t_yunai")))
                .thenReturn(tableInfo);
        when(tableInfo.getComment()).thenReturn("芋艿");
        // mock 方法（TableInfo fields）
        TableField field01 = mock(TableField.class);
        when(field01.getComment()).thenReturn("主键");
        TableField field02 = mock(TableField.class);
        when(field02.getComment()).thenReturn("名字");
        List<TableField> fields = Arrays.asList(field01, field02);
        when(tableInfo.getFields()).thenReturn(fields);
        // mock 方法（CodegenTableDO）
        CodegenTableDO table = randomPojo(CodegenTableDO.class);
        when(codegenBuilder.buildTable(same(tableInfo))).thenReturn(table);
        // mock 方法（AdminUserRespDTO）
        AdminUserRespDTO user = randomPojo(AdminUserRespDTO.class, o -> o.setNickname("芋头"));
        when(userApi.getUser(eq(userId))).thenReturn(user);
        // mock 方法（CodegenColumnDO）
        List<CodegenColumnDO> columns = randomPojoList(CodegenColumnDO.class);
        when(codegenBuilder.buildColumns(eq(table.getId()), same(fields)))
                .thenReturn(columns);
        // mock 方法（CodegenProperties）
        when(codegenProperties.getFrontType()).thenReturn(CodegenFrontTypeEnum.VUE3.getType());

        // 调用
        List<Long> result = codegenService.createCodegenList(userId, reqVO);
        // 断言
        assertEquals(1, result.size());
        // 断言（CodegenTableDO）
        CodegenTableDO dbTable = codegenTableMapper.selectList().get(0);
        assertPojoEquals(table, dbTable);
        assertEquals(1L, dbTable.getDataSourceConfigId());
        assertEquals(CodegenSceneEnum.ADMIN.getScene(), dbTable.getScene());
        assertEquals(CodegenFrontTypeEnum.VUE3.getType(), dbTable.getFrontType());
        assertEquals("芋头", dbTable.getAuthor());
        // 断言（CodegenColumnDO）
        List<CodegenColumnDO> dbColumns = codegenColumnMapper.selectList();
        assertEquals(columns.size(), dbColumns.size());
        assertTrue(dbColumns.get(0).getPrimaryKey());
        for (int i = 0; i < dbColumns.size(); i++) {
            assertPojoEquals(columns.get(i), dbColumns.get(i));
        }
    }

    @Test
    public void testValidateTableInfo() {
        // 情况一
        assertServiceException(() -> codegenService.validateTableInfo(null),
                CODEGEN_IMPORT_TABLE_NULL);
        // 情况二
        TableInfo tableInfo = mock(TableInfo.class);
        assertServiceException(() -> codegenService.validateTableInfo(tableInfo),
                CODEGEN_TABLE_INFO_TABLE_COMMENT_IS_NULL);
        // 情况三
        when(tableInfo.getComment()).thenReturn("芋艿");
        assertServiceException(() -> codegenService.validateTableInfo(tableInfo),
                CODEGEN_IMPORT_COLUMNS_NULL);
        // 情况四
        TableField field = mock(TableField.class);
        when(field.getName()).thenReturn("name");
        when(tableInfo.getFields()).thenReturn(Collections.singletonList(field));
        assertServiceException(() -> codegenService.validateTableInfo(tableInfo),
                CODEGEN_TABLE_INFO_COLUMN_COMMENT_IS_NULL, field.getName());
    }

    @Test
    public void testUpdateCodegen_notExists() {
        // 准备参数
        CodegenUpdateReqVO updateReqVO = randomPojo(CodegenUpdateReqVO.class);
        // mock 方法

        // 调用，并断言
        assertServiceException(() -> codegenService.updateCodegen(updateReqVO),
                CODEGEN_TABLE_NOT_EXISTS);
    }

    @Test
    public void testUpdateCodegen_sub_masterNotExists() {
        // mock 数据
        CodegenTableDO table = randomPojo(CodegenTableDO.class,
                o -> o.setTemplateType(CodegenTemplateTypeEnum.SUB.getType())
                        .setScene(CodegenSceneEnum.ADMIN.getScene()));
        codegenTableMapper.insert(table);
        // 准备参数
        CodegenUpdateReqVO updateReqVO = randomPojo(CodegenUpdateReqVO.class,
                o -> o.getTable().setId(table.getId())
                        .setTemplateType(CodegenTemplateTypeEnum.SUB.getType()));

        // 调用，并断言
        assertServiceException(() -> codegenService.updateCodegen(updateReqVO),
                CODEGEN_MASTER_TABLE_NOT_EXISTS, updateReqVO.getTable().getMasterTableId());
    }

    @Test
    public void testUpdateCodegen_sub_columnNotExists() {
        // mock 数据
        CodegenTableDO subTable = randomPojo(CodegenTableDO.class,
                o -> o.setTemplateType(CodegenTemplateTypeEnum.SUB.getType())
                        .setScene(CodegenSceneEnum.ADMIN.getScene()));
        codegenTableMapper.insert(subTable);
        // mock 数据（master）
        CodegenTableDO masterTable = randomPojo(CodegenTableDO.class,
                o -> o.setTemplateType(CodegenTemplateTypeEnum.MASTER_ERP.getType())
                        .setScene(CodegenSceneEnum.ADMIN.getScene()));
        codegenTableMapper.insert(masterTable);
        // 准备参数
        CodegenUpdateReqVO updateReqVO = randomPojo(CodegenUpdateReqVO.class,
                o -> o.getTable().setId(subTable.getId())
                        .setTemplateType(CodegenTemplateTypeEnum.SUB.getType())
                        .setMasterTableId(masterTable.getId()));

        // 调用，并断言
        assertServiceException(() -> codegenService.updateCodegen(updateReqVO),
                CODEGEN_SUB_COLUMN_NOT_EXISTS, updateReqVO.getTable().getSubJoinColumnId());
    }

    @Test
    public void testUpdateCodegen_success() {
        // mock 数据
        CodegenTableDO table = randomPojo(CodegenTableDO.class,
                o -> o.setTemplateType(CodegenTemplateTypeEnum.ONE.getType())
                        .setScene(CodegenSceneEnum.ADMIN.getScene()));
        codegenTableMapper.insert(table);
        CodegenColumnDO column01 = randomPojo(CodegenColumnDO.class, o -> o.setTableId(table.getId()));
        codegenColumnMapper.insert(column01);
        CodegenColumnDO column02 = randomPojo(CodegenColumnDO.class, o -> o.setTableId(table.getId()));
        codegenColumnMapper.insert(column02);
        // 准备参数
        CodegenUpdateReqVO updateReqVO = randomPojo(CodegenUpdateReqVO.class,
                o -> o.getTable().setId(table.getId())
                        .setTemplateType(CodegenTemplateTypeEnum.ONE.getType())
                        .setScene(CodegenSceneEnum.ADMIN.getScene()));
        CodegenColumnSaveReqVO columnVO01 = randomPojo(CodegenColumnSaveReqVO.class,
                o -> o.setId(column01.getId()).setTableId(table.getId()));
        CodegenColumnSaveReqVO columnVO02 = randomPojo(CodegenColumnSaveReqVO.class,
                o -> o.setId(column02.getId()).setTableId(table.getId()));
        updateReqVO.setColumns(Arrays.asList(columnVO01, columnVO02));

        // 调用
        codegenService.updateCodegen(updateReqVO);
        // 断言
        CodegenTableDO dbTable = codegenTableMapper.selectById(table.getId());
        assertPojoEquals(updateReqVO.getTable(), dbTable);
        List<CodegenColumnDO> dbColumns = codegenColumnMapper.selectList();
        assertEquals(2, dbColumns.size());
        assertPojoEquals(columnVO01, dbColumns.get(0));
        assertPojoEquals(columnVO02, dbColumns.get(1));
    }

    @Test
    public void testSyncCodegenFromDB() {
        // mock 数据（CodegenTableDO）
        CodegenTableDO table = randomPojo(CodegenTableDO.class, o -> o.setTableName("t_yunai")
                .setDataSourceConfigId(1L).setScene(CodegenSceneEnum.ADMIN.getScene()));
        codegenTableMapper.insert(table);
        CodegenColumnDO column01 = randomPojo(CodegenColumnDO.class, o -> o.setTableId(table.getId())
                .setColumnName("id").setPrimaryKey(true).setOrdinalPosition(0));
        codegenColumnMapper.insert(column01);
        CodegenColumnDO column02 = randomPojo(CodegenColumnDO.class, o -> o.setTableId(table.getId())
                .setColumnName("name").setOrdinalPosition(1));
        codegenColumnMapper.insert(column02);
        // 准备参数
        Long tableId = table.getId();
        // mock 方法（TableInfo）
        TableInfo tableInfo = mock(TableInfo.class);
        when(databaseTableService.getTable(eq(1L), eq("t_yunai")))
                .thenReturn(tableInfo);
        when(tableInfo.getComment()).thenReturn("芋艿");
        // mock 方法（TableInfo fields）
        TableField field01 = mock(TableField.class);
        when(field01.getComment()).thenReturn("主键");
        TableField field03 = mock(TableField.class);
        when(field03.getComment()).thenReturn("分类");
        List<TableField> fields = Arrays.asList(field01, field03);
        when(tableInfo.getFields()).thenReturn(fields);
        when(databaseTableService.getTable(eq(1L), eq("t_yunai")))
                .thenReturn(tableInfo);
        // mock 方法（CodegenTableDO）
        List<CodegenColumnDO> newColumns = randomPojoList(CodegenColumnDO.class, 2);
        when(codegenBuilder.buildColumns(eq(table.getId()), argThat(tableFields -> {
            assertEquals(2, tableFields.size());
            assertSame(tableInfo.getFields(), tableFields);
            return true;
        }))).thenReturn(newColumns);

        // 调用
        codegenService.syncCodegenFromDB(tableId);
        // 断言
        List<CodegenColumnDO> dbColumns = codegenColumnMapper.selectList();
        assertEquals(newColumns.size(), dbColumns.size());
        assertPojoEquals(newColumns.get(0), dbColumns.get(0));
        assertPojoEquals(newColumns.get(1), dbColumns.get(1));
    }

    @Test
    public void testDeleteCodegen_notExists() {
        assertServiceException(() -> codegenService.deleteCodegen(randomLongId()),
                CODEGEN_TABLE_NOT_EXISTS);
    }

    @Test
    public void testDeleteCodegen_success() {
        // mock 数据
        CodegenTableDO table = randomPojo(CodegenTableDO.class,
                o -> o.setScene(CodegenSceneEnum.ADMIN.getScene()));
        codegenTableMapper.insert(table);
        CodegenColumnDO column = randomPojo(CodegenColumnDO.class, o -> o.setTableId(table.getId()));
        codegenColumnMapper.insert(column);
        // 准备参数
        Long tableId = table.getId();

        // 调用
        codegenService.deleteCodegen(tableId);
        // 断言
        assertNull(codegenTableMapper.selectById(tableId));
        assertEquals(0, codegenColumnMapper.selectList().size());
    }

    @Test
    public void testGetCodegenTableList() {
        // mock 数据
        CodegenTableDO table01 = randomPojo(CodegenTableDO.class,
                o -> o.setScene(CodegenSceneEnum.ADMIN.getScene()));
        codegenTableMapper.insert(table01);
        CodegenTableDO table02 = randomPojo(CodegenTableDO.class,
                o -> o.setScene(CodegenSceneEnum.ADMIN.getScene()));
        codegenTableMapper.insert(table02);
        // 准备参数
        Long dataSourceConfigId = table01.getDataSourceConfigId();

        // 调用
        List<CodegenTableDO> result = codegenService.getCodegenTableList(dataSourceConfigId);
        // 断言
        assertEquals(1, result.size());
        assertPojoEquals(table01, result.get(0));
    }

    @Test
    public void testGetCodegenTablePage() {
        // mock 数据
        CodegenTableDO tableDO = randomPojo(CodegenTableDO.class, o -> {
            o.setTableName("t_yunai");
            o.setTableComment("芋艿");
            o.setClassName("SystemYunai");
            o.setCreateTime(buildTime(2021, 3, 10));
        }).setScene(CodegenSceneEnum.ADMIN.getScene());
        codegenTableMapper.insert(tableDO);
        // 测试 tableName 不匹配
        codegenTableMapper.insert(cloneIgnoreId(tableDO, o -> o.setTableName(randomString())));
        // 测试 tableComment 不匹配
        codegenTableMapper.insert(cloneIgnoreId(tableDO, o -> o.setTableComment(randomString())));
        // 测试 className 不匹配
        codegenTableMapper.insert(cloneIgnoreId(tableDO, o -> o.setClassName(randomString())));
        // 测试 createTime 不匹配
        codegenTableMapper.insert(cloneIgnoreId(tableDO, logDO -> logDO.setCreateTime(buildTime(2021, 4, 10))));
        // 准备参数
        CodegenTablePageReqVO reqVO = new CodegenTablePageReqVO();
        reqVO.setTableName("yunai");
        reqVO.setTableComment("芋");
        reqVO.setClassName("Yunai");
        reqVO.setCreateTime(buildBetweenTime(2021, 3, 1, 2021, 3, 31));

        // 调用
        PageResult<CodegenTableDO> pageResult = codegenService.getCodegenTablePage(reqVO);
        // 断言，只查到了一条符合条件的
        assertEquals(1, pageResult.getTotal());
        assertEquals(1, pageResult.getList().size());
        assertPojoEquals(tableDO, pageResult.getList().get(0));
    }

    @Test
    public void testGetCodegenTable() {
        // mock 数据
        CodegenTableDO tableDO = randomPojo(CodegenTableDO.class, o -> o.setScene(CodegenSceneEnum.ADMIN.getScene()));
        codegenTableMapper.insert(tableDO);
        // 准备参数
        Long id = tableDO.getId();

        // 调用
        CodegenTableDO result = codegenService.getCodegenTable(id);
        // 断言
        assertPojoEquals(tableDO, result);
    }

    @Test
    public void testGetCodegenColumnListByTableId() {
        // mock 数据
        CodegenColumnDO column01 = randomPojo(CodegenColumnDO.class);
        codegenColumnMapper.insert(column01);
        CodegenColumnDO column02 = randomPojo(CodegenColumnDO.class);
        codegenColumnMapper.insert(column02);
        // 准备参数
        Long tableId = column01.getTableId();

        // 调用
        List<CodegenColumnDO> result = codegenService.getCodegenColumnListByTableId(tableId);
        // 断言
        assertEquals(1, result.size());
        assertPojoEquals(column01, result.get(0));
    }

    @Test
    public void testGenerationCodes_tableNotExists() {
        assertServiceException(() -> codegenService.generationCodes(randomLongId()),
                CODEGEN_TABLE_NOT_EXISTS);
    }

    @Test
    public void testGenerationCodes_columnNotExists() {
        // mock 数据（CodegenTableDO）
        CodegenTableDO table = randomPojo(CodegenTableDO.class,
                o -> o.setScene(CodegenSceneEnum.ADMIN.getScene())
                        .setTemplateType(CodegenTemplateTypeEnum.MASTER_NORMAL.getType()));
        codegenTableMapper.insert(table);
        // 准备参数
        Long tableId = table.getId();

        // 调用，并断言
        assertServiceException(() -> codegenService.generationCodes(tableId),
                CODEGEN_COLUMN_NOT_EXISTS);
    }

    @Test
    public void testGenerationCodes_sub_tableNotExists() {
        // mock 数据（CodegenTableDO）
        CodegenTableDO table = randomPojo(CodegenTableDO.class,
                o -> o.setScene(CodegenSceneEnum.ADMIN.getScene())
                        .setTemplateType(CodegenTemplateTypeEnum.MASTER_NORMAL.getType()));
        codegenTableMapper.insert(table);
        // mock 数据（CodegenColumnDO）
        CodegenColumnDO column01 = randomPojo(CodegenColumnDO.class, o -> o.setTableId(table.getId()));
        codegenColumnMapper.insert(column01);
        // 准备参数
        Long tableId = table.getId();

        // 调用，并断言
        assertServiceException(() -> codegenService.generationCodes(tableId),
                CODEGEN_MASTER_GENERATION_FAIL_NO_SUB_TABLE);
    }

    @Test
    public void testGenerationCodes_sub_columnNotExists() {
        // mock 数据（CodegenTableDO）
        CodegenTableDO table = randomPojo(CodegenTableDO.class,
                o -> o.setScene(CodegenSceneEnum.ADMIN.getScene())
                        .setTemplateType(CodegenTemplateTypeEnum.MASTER_NORMAL.getType()));
        codegenTableMapper.insert(table);
        // mock 数据（CodegenColumnDO）
        CodegenColumnDO column01 = randomPojo(CodegenColumnDO.class, o -> o.setTableId(table.getId()));
        codegenColumnMapper.insert(column01);
        // mock 数据（sub CodegenTableDO）
        CodegenTableDO subTable = randomPojo(CodegenTableDO.class,
                o -> o.setScene(CodegenSceneEnum.ADMIN.getScene())
                        .setTemplateType(CodegenTemplateTypeEnum.SUB.getType())
                        .setMasterTableId(table.getId()));
        codegenTableMapper.insert(subTable);
        // 准备参数
        Long tableId = table.getId();

        // 调用，并断言
        assertServiceException(() -> codegenService.generationCodes(tableId),
                CODEGEN_SUB_COLUMN_NOT_EXISTS, subTable.getId());
    }

    @Test
    public void testGenerationCodes_one_success() {
        // mock 数据（CodegenTableDO）
        CodegenTableDO table = randomPojo(CodegenTableDO.class,
                o -> o.setScene(CodegenSceneEnum.ADMIN.getScene())
                        .setTemplateType(CodegenTemplateTypeEnum.ONE.getType()));
        codegenTableMapper.insert(table);
        // mock 数据（CodegenColumnDO）
        CodegenColumnDO column01 = randomPojo(CodegenColumnDO.class, o -> o.setTableId(table.getId())
                .setOrdinalPosition(1));
        codegenColumnMapper.insert(column01);
        CodegenColumnDO column02 = randomPojo(CodegenColumnDO.class, o -> o.setTableId(table.getId())
                .setOrdinalPosition(2));
        codegenColumnMapper.insert(column02);
        // mock 执行生成
        Map<String, String> codes = MapUtil.of(randomString(), randomString());
        when(codegenEngine.execute(eq(table), argThat(columns -> {
            assertEquals(2, columns.size());
            assertEquals(column01, columns.get(0));
            assertEquals(column02, columns.get(1));
            return true;
        }), isNull(), isNull())).thenReturn(codes);
        // 准备参数
        Long tableId = table.getId();

        // 调用
        Map<String, String> result = codegenService.generationCodes(tableId);
        // 断言
        assertSame(codes, result);
    }

    @Test
    public void testGenerationCodes_master_success() {
        // mock 数据（CodegenTableDO）
        CodegenTableDO table = randomPojo(CodegenTableDO.class,
                o -> o.setScene(CodegenSceneEnum.ADMIN.getScene())
                        .setTemplateType(CodegenTemplateTypeEnum.MASTER_NORMAL.getType()));
        codegenTableMapper.insert(table);
        // mock 数据（CodegenColumnDO）
        CodegenColumnDO column01 = randomPojo(CodegenColumnDO.class, o -> o.setTableId(table.getId())
                .setOrdinalPosition(1));
        codegenColumnMapper.insert(column01);
        CodegenColumnDO column02 = randomPojo(CodegenColumnDO.class, o -> o.setTableId(table.getId())
                .setOrdinalPosition(2));
        codegenColumnMapper.insert(column02);
        // mock 数据（sub CodegenTableDO）
        CodegenTableDO subTable = randomPojo(CodegenTableDO.class,
                o -> o.setScene(CodegenSceneEnum.ADMIN.getScene())
                        .setTemplateType(CodegenTemplateTypeEnum.SUB.getType())
                        .setMasterTableId(table.getId())
                        .setSubJoinColumnId(1024L));
        codegenTableMapper.insert(subTable);
        // mock 数据（sub CodegenColumnDO）
        CodegenColumnDO subColumn01 = randomPojo(CodegenColumnDO.class, o -> o.setId(1024L).setTableId(subTable.getId()));
        codegenColumnMapper.insert(subColumn01);
        // mock 执行生成
        Map<String, String> codes = MapUtil.of(randomString(), randomString());
        when(codegenEngine.execute(eq(table), argThat(columns -> {
            assertEquals(2, columns.size());
            assertEquals(column01, columns.get(0));
            assertEquals(column02, columns.get(1));
            return true;
        }), argThat(tables -> {
            assertEquals(1, tables.size());
            assertPojoEquals(subTable, tables.get(0));
            return true;
        }), argThat(columns -> {
            assertEquals(1, columns.size());
            assertPojoEquals(subColumn01, columns.size());
            return true;
        }))).thenReturn(codes);
        // 准备参数
        Long tableId = table.getId();

        // 调用
        Map<String, String> result = codegenService.generationCodes(tableId);
        // 断言
        assertSame(codes, result);
    }

    @Test
    public void testGetDatabaseTableList() {
        // 准备参数
        Long dataSourceConfigId = randomLongId();
        String name = randomString();
        String comment = randomString();
        // mock 方法
        TableInfo tableInfo01 = mock(TableInfo.class);
        when(tableInfo01.getName()).thenReturn("t_yunai");
        when(tableInfo01.getComment()).thenReturn("芋艿");
        TableInfo tableInfo02 = mock(TableInfo.class);
        when(tableInfo02.getName()).thenReturn("t_yunai_02");
        when(tableInfo02.getComment()).thenReturn("芋艿_02");
        when(databaseTableService.getTableList(eq(dataSourceConfigId), eq(name), eq(comment)))
                .thenReturn(ListUtil.toList(tableInfo01, tableInfo02));
        // mock 数据
        CodegenTableDO tableDO = randomPojo(CodegenTableDO.class,
                o -> o.setScene(CodegenSceneEnum.ADMIN.getScene())
                        .setTableName("t_yunai_02")
                        .setDataSourceConfigId(dataSourceConfigId));
        codegenTableMapper.insert(tableDO);

        // 调用
        List<DatabaseTableRespVO> result = codegenService.getDatabaseTableList(dataSourceConfigId, name, comment);
        // 断言
        assertEquals(1, result.size());
        assertEquals("t_yunai", result.get(0).getName());
        assertEquals("芋艿", result.get(0).getComment());
    }

}