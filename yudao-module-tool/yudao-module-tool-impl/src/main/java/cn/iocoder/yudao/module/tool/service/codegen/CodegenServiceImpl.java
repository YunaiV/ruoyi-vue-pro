package cn.iocoder.yudao.module.tool.service.codegen;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.tool.framework.codegen.config.CodegenProperties;
import cn.iocoder.yudao.module.tool.controller.admin.codegen.vo.CodegenUpdateReqVO;
import cn.iocoder.yudao.module.tool.controller.admin.codegen.vo.table.CodegenTablePageReqVO;
import cn.iocoder.yudao.module.tool.convert.codegen.CodegenConvert;
import cn.iocoder.yudao.module.tool.dal.dataobject.codegen.CodegenColumnDO;
import cn.iocoder.yudao.module.tool.dal.dataobject.codegen.CodegenTableDO;
import cn.iocoder.yudao.module.tool.dal.dataobject.codegen.SchemaColumnDO;
import cn.iocoder.yudao.module.tool.dal.dataobject.codegen.SchemaTableDO;
import cn.iocoder.yudao.module.tool.dal.mysql.codegen.CodegenColumnMapper;
import cn.iocoder.yudao.module.tool.dal.mysql.codegen.CodegenTableMapper;
import cn.iocoder.yudao.module.tool.dal.mysql.codegen.SchemaColumnMapper;
import cn.iocoder.yudao.module.tool.dal.mysql.codegen.SchemaTableMapper;
import cn.iocoder.yudao.module.tool.enums.codegen.CodegenImportTypeEnum;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.module.tool.service.codegen.inner.CodegenBuilder;
import cn.iocoder.yudao.module.tool.service.codegen.inner.CodegenEngine;
import cn.iocoder.yudao.module.tool.service.codegen.inner.CodegenSQLParser;
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
import static cn.iocoder.yudao.module.tool.enums.ErrorCodeConstants.*;

/**
 * 代码生成 Service 实现类
 *
 * @author 芋道源码
 */
@Service
public class CodegenServiceImpl implements CodegenService {

    @Resource
    private SchemaTableMapper schemaTableMapper;
    @Resource
    private SchemaColumnMapper schemaColumnMapper;
    @Resource
    private CodegenTableMapper codegenTableMapper;
    @Resource
    private CodegenColumnMapper codegenColumnMapper;

    @Resource
    private CodegenBuilder codegenBuilder;
    @Resource
    private CodegenEngine codegenEngine;

    @Resource
    private CodegenProperties codegenProperties;

    private Long createCodegen0(CodegenImportTypeEnum importType,
                                SchemaTableDO schemaTable, List<SchemaColumnDO> schemaColumns) {
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
        codegenTableMapper.insert(table);
        // 构建 CodegenColumnDO 数组，插入到 DB 中
        List<CodegenColumnDO> columns = codegenBuilder.buildColumns(schemaColumns);
        columns.forEach(column -> {
            column.setTableId(table.getId());
            codegenColumnMapper.insert(column); // TODO 批量插入
        });
        return table.getId();
    }

    @Override
    public Long createCodegenListFromSQL(String sql) {
        // 从 SQL 中，获得数据库表结构
        SchemaTableDO schemaTable;
        List<SchemaColumnDO> schemaColumns;
        try {
            KeyValue<SchemaTableDO, List<SchemaColumnDO>> result = CodegenSQLParser.parse(sql);
            schemaTable = result.getKey();
            schemaColumns = result.getValue();
        } catch (Exception ex) {
            throw exception(CODEGEN_PARSE_SQL_ERROR);
        }
        // 导入
        return this.createCodegen0(CodegenImportTypeEnum.SQL, schemaTable, schemaColumns);
    }

    @Override
    public Long createCodegen(String tableName) {
        // 获取当前schema
        String tableSchema = codegenProperties.getDbSchemas().iterator().next();
        // 从数据库中，获得数据库表结构
        SchemaTableDO schemaTable = schemaTableMapper.selectByTableSchemaAndTableName(tableSchema, tableName);
        List<SchemaColumnDO> schemaColumns = schemaColumnMapper.selectListByTableName(tableSchema, tableName);
        // 导入
        return this.createCodegen0(CodegenImportTypeEnum.DB, schemaTable, schemaColumns);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<Long> createCodegenListFromDB(List<String> tableNames) {
        List<Long> ids = new ArrayList<>(tableNames.size());
        // 遍历添加。虽然效率会低一点，但是没必要做成完全批量，因为不会这么大量
        tableNames.forEach(tableName -> ids.add(createCodegen(tableName)));
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
        String tableSchema = codegenProperties.getDbSchemas().iterator().next();
        // 从数据库中，获得数据库表结构
        List<SchemaColumnDO> schemaColumns = schemaColumnMapper.selectListByTableName(tableSchema, table.getTableName());

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
        List<SchemaColumnDO> schemaColumns;
        try {
            KeyValue<SchemaTableDO, List<SchemaColumnDO>> result = CodegenSQLParser.parse(sql);
            schemaColumns = result.getValue();
        } catch (Exception ex) {
            throw exception(CODEGEN_PARSE_SQL_ERROR);
        }

        // 执行同步
        this.syncCodegen0(tableId, schemaColumns);
    }

    private void syncCodegen0(Long tableId, List<SchemaColumnDO> schemaColumns) {
        // 校验导入的字段不为空
        if (CollUtil.isEmpty(schemaColumns)) {
            throw exception(CODEGEN_SYNC_COLUMNS_NULL);
        }
        Set<String> schemaColumnNames = CollectionUtils.convertSet(schemaColumns, SchemaColumnDO::getColumnName);

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
        List<CodegenColumnDO> columns = codegenBuilder.buildColumns(schemaColumns);
        columns.forEach(column -> {
            column.setTableId(tableId);
            codegenColumnMapper.insert(column); // TODO 批量插入
        });
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
    public List<SchemaTableDO> getSchemaTableList(String tableName, String tableComment) {
        List<SchemaTableDO> tables = schemaTableMapper.selectList(codegenProperties.getDbSchemas(), tableName, tableComment);
        // TODO 强制移除 Quartz 的表，未来做成可配置
        tables.removeIf(table -> table.getTableName().startsWith("QRTZ_"));
        tables.removeIf(table -> table.getTableName().startsWith("ACT_"));
        return tables;
    }

//    /**
//     * 修改保存参数校验
//     *
//     * @param genTable 业务信息
//     */
//    @Override
//    public void validateEdit(GenTable genTable) {
//        if (GenConstants.TPL_TREE.equals(genTable.getTplCategory())) {
//            String options = JSON.toJSONString(genTable.getParams());
//            JSONObject paramsObj = JSONObject.parseObject(options);
//            if (StringUtils.isEmpty(paramsObj.getString(GenConstants.TREE_CODE))) {
//                throw new CustomException("树编码字段不能为空");
//            } else if (StringUtils.isEmpty(paramsObj.getString(GenConstants.TREE_PARENT_CODE))) {
//                throw new CustomException("树父编码字段不能为空");
//            } else if (StringUtils.isEmpty(paramsObj.getString(GenConstants.TREE_NAME))) {
//                throw new CustomException("树名称字段不能为空");
//            }
//        }
//    }

}
