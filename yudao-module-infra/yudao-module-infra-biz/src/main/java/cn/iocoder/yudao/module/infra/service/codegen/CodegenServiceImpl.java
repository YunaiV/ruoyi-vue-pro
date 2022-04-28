package cn.iocoder.yudao.module.infra.service.codegen;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.module.infra.controller.admin.codegen.vo.CodegenUpdateReqVO;
import cn.iocoder.yudao.module.infra.controller.admin.codegen.vo.table.CodegenTablePageReqVO;
import cn.iocoder.yudao.module.infra.convert.codegen.CodegenConvert;
import cn.iocoder.yudao.module.infra.dal.dataobject.codegen.CodegenColumnDO;
import cn.iocoder.yudao.module.infra.dal.dataobject.codegen.CodegenTableDO;
import cn.iocoder.yudao.module.infra.dal.dataobject.db.DatabaseColumnDO;
import cn.iocoder.yudao.module.infra.dal.dataobject.db.DatabaseTableDO;
import cn.iocoder.yudao.module.infra.dal.mysql.codegen.CodegenColumnMapper;
import cn.iocoder.yudao.module.infra.dal.mysql.codegen.CodegenTableMapper;
import cn.iocoder.yudao.module.infra.enums.codegen.CodegenImportTypeEnum;
import cn.iocoder.yudao.module.infra.framework.codegen.config.CodegenProperties;
import cn.iocoder.yudao.module.infra.service.codegen.inner.CodegenBuilder;
import cn.iocoder.yudao.module.infra.service.codegen.inner.CodegenEngine;
import cn.iocoder.yudao.module.infra.service.codegen.inner.CodegenSQLParser;
import cn.iocoder.yudao.module.infra.service.db.DatabaseTableService;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import org.apache.commons.collections4.KeyValue;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.infra.enums.ErrorCodeConstants.*;

/**
 * 代码生成 Service 实现类
 *
 * @author 芋道源码
 */
@Service
public class CodegenServiceImpl implements CodegenService {

    @Resource
    private DatabaseTableService databaseTableService;

    @Resource
    private CodegenTableMapper codegenTableMapper;
    @Resource
    private CodegenColumnMapper codegenColumnMapper;

    @Resource
    private AdminUserApi userApi;

    @Resource
    private CodegenBuilder codegenBuilder;
    @Resource
    private CodegenEngine codegenEngine;

    private Long createCodegen0(Long userId, CodegenImportTypeEnum importType,
                                DatabaseTableDO schemaTable, List<DatabaseColumnDO> schemaColumns) {
        // 校验导入的表和字段非空
        if (schemaTable == null) {
            throw exception(CODEGEN_IMPORT_TABLE_NULL);
        }
        if (CollUtil.isEmpty(schemaColumns)) {
            throw exception(CODEGEN_IMPORT_COLUMNS_NULL);
        }
        // 校验是否已经存在
        if (codegenTableMapper.selectByTableName(schemaTable.getTableName()) != null) {
            throw exception(CODEGEN_TABLE_EXISTS);
        }

        // 构建 CodegenTableDO 对象，插入到 DB 中
        CodegenTableDO table = codegenBuilder.buildTable(schemaTable);
        table.setImportType(importType.getType());
        table.setAuthor(userApi.getUser(userId).getNickname());
        codegenTableMapper.insert(table);
        // 构建 CodegenColumnDO 数组，插入到 DB 中
        List<CodegenColumnDO> columns = codegenBuilder.buildColumns(table.getId(), schemaColumns);
        codegenColumnMapper.insertBatch(columns);
        return table.getId();
    }

    @Override
    public Long createCodegenListFromSQL(Long userId, String sql) {
        // 从 SQL 中，获得数据库表结构
        DatabaseTableDO schemaTable;
        List<DatabaseColumnDO> schemaColumns;
        try {
            KeyValue<DatabaseTableDO, List<DatabaseColumnDO>> result = CodegenSQLParser.parse(sql);
            schemaTable = result.getKey();
            schemaColumns = result.getValue();
        } catch (Exception ex) {
            throw exception(CODEGEN_PARSE_SQL_ERROR);
        }
        // 导入
        return this.createCodegen0(userId, CodegenImportTypeEnum.SQL, schemaTable, schemaColumns);
    }

    @Override
    public Long createCodegen(Long userId, String tableName) {
        // 从数据库中，获得数据库表结构
        DatabaseTableDO schemaTable = databaseTableService.getTable(9L, tableName);
        List<DatabaseColumnDO> schemaColumns = databaseTableService.getColumnList(9L, tableName);
        // 导入
        return this.createCodegen0(userId, CodegenImportTypeEnum.DB, schemaTable, schemaColumns);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<Long> createCodegenListFromDB(Long userId, List<String> tableNames) {
        List<Long> ids = new ArrayList<>(tableNames.size());
        // 遍历添加。虽然效率会低一点，但是没必要做成完全批量，因为不会这么大量
        tableNames.forEach(tableName -> ids.add(createCodegen(userId, tableName)));
        return ids;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCodegen(CodegenUpdateReqVO updateReqVO) {
        // 校验是否已经存在
        if (codegenTableMapper.selectById(updateReqVO.getTable().getId()) == null) {
            throw exception(CODEGEN_TABLE_NOT_EXISTS);
        }

        // 更新 table 表定义
        CodegenTableDO updateTableObj = CodegenConvert.INSTANCE.convert(updateReqVO.getTable());
        codegenTableMapper.updateById(updateTableObj);
        // 更新 column 字段定义
        List<CodegenColumnDO> updateColumnObjs = CodegenConvert.INSTANCE.convertList03(updateReqVO.getColumns());
        updateColumnObjs.forEach(updateColumnObj -> codegenColumnMapper.updateById(updateColumnObj));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncCodegenFromDB(Long tableId) {
        // 校验是否已经存在
        CodegenTableDO table = codegenTableMapper.selectById(tableId);
        if (table == null) {
            throw exception(CODEGEN_TABLE_NOT_EXISTS);
        }
        // 从数据库中，获得数据库表结构
        List<DatabaseColumnDO> schemaColumns = databaseTableService.getColumnList(0L, table.getTableName());

        // 执行同步
        this.syncCodegen0(tableId, schemaColumns);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncCodegenFromSQL(Long tableId, String sql) {
        // 校验是否已经存在
        CodegenTableDO table = codegenTableMapper.selectById(tableId);
        if (table == null) {
            throw exception(CODEGEN_TABLE_NOT_EXISTS);
        }
        // 从 SQL 中，获得数据库表结构
        List<DatabaseColumnDO> schemaColumns;
        try {
            KeyValue<DatabaseTableDO, List<DatabaseColumnDO>> result = CodegenSQLParser.parse(sql);
            schemaColumns = result.getValue();
        } catch (Exception ex) {
            throw exception(CODEGEN_PARSE_SQL_ERROR);
        }

        // 执行同步
        this.syncCodegen0(tableId, schemaColumns);
    }

    private void syncCodegen0(Long tableId, List<DatabaseColumnDO> schemaColumns) {
        // 校验导入的字段不为空
        if (CollUtil.isEmpty(schemaColumns)) {
            throw exception(CODEGEN_SYNC_COLUMNS_NULL);
        }
        Set<String> schemaColumnNames = CollectionUtils.convertSet(schemaColumns, DatabaseColumnDO::getColumnName);

        // 构建 CodegenColumnDO 数组，只同步新增的字段
        List<CodegenColumnDO> codegenColumns = codegenColumnMapper.selectListByTableId(tableId);
        Set<String> codegenColumnNames = CollectionUtils.convertSet(codegenColumns, CodegenColumnDO::getColumnName);
        // 移除已经存在的字段
        schemaColumns.removeIf(column -> codegenColumnNames.contains(column.getColumnName()));
        // 计算需要删除的字段
        Set<Long> deleteColumnIds = codegenColumns.stream().filter(column -> !schemaColumnNames.contains(column.getColumnName()))
                .map(CodegenColumnDO::getId).collect(Collectors.toSet());
        if (CollUtil.isEmpty(schemaColumns) && CollUtil.isEmpty(deleteColumnIds)) {
            throw exception(CODEGEN_SYNC_NONE_CHANGE);
        }

        // 插入新增的字段
        List<CodegenColumnDO> columns = codegenBuilder.buildColumns(tableId, schemaColumns);
        codegenColumnMapper.insertBatch(columns);
        // 删除不存在的字段
        if (CollUtil.isNotEmpty(deleteColumnIds)) {
            codegenColumnMapper.deleteBatchIds(deleteColumnIds);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCodegen(Long tableId) {
        // 校验是否已经存在
        if (codegenTableMapper.selectById(tableId) == null) {
            throw exception(CODEGEN_TABLE_NOT_EXISTS);
        }

        // 删除 table 表定义
        codegenTableMapper.deleteById(tableId);
        // 删除 column 字段定义
        codegenColumnMapper.deleteListByTableId(tableId);
    }

    @Override
    public PageResult<CodegenTableDO> getCodegenTablePage(CodegenTablePageReqVO pageReqVO) {
        return codegenTableMapper.selectPage(pageReqVO);
    }

    @Override
    public CodegenTableDO getCodegenTablePage(Long id) {
        return codegenTableMapper.selectById(id);
    }

    @Override
    public List<CodegenTableDO> getCodeGenTableList() {
        return codegenTableMapper.selectList();
    }

    @Override
    public List<CodegenColumnDO> getCodegenColumnListByTableId(Long tableId) {
        return codegenColumnMapper.selectListByTableId(tableId);
    }

    @Override
    public Map<String, String> generationCodes(Long tableId) {
        // 校验是否已经存在
        CodegenTableDO table = codegenTableMapper.selectById(tableId);
        if (codegenTableMapper.selectById(tableId) == null) {
            throw exception(CODEGEN_TABLE_NOT_EXISTS);
        }
        List<CodegenColumnDO> columns = codegenColumnMapper.selectListByTableId(tableId);
        if (CollUtil.isEmpty(columns)) {
            throw exception(CODEGEN_COLUMN_NOT_EXISTS);
        }

        // 执行生成
        return codegenEngine.execute(table, columns);
    }

    @Override
    public List<DatabaseTableDO> getSchemaTableList(Long dataSourceConfigId, String tableName, String tableComment) {
        List<DatabaseTableDO> tables = databaseTableService.getTableList(dataSourceConfigId, tableName, tableComment);
        // TODO 强制移除 Quartz 的表，未来做成可配置
        tables.removeIf(table -> table.getTableName().startsWith("QRTZ_"));
        tables.removeIf(table -> table.getTableName().startsWith("ACT_"));
        tables.removeIf(table -> table.getTableName().startsWith("FLW_"));
        return tables;
    }

}
