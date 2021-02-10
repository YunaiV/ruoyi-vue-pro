package cn.iocoder.dashboard.modules.tool.service.codegen.impl;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.dashboard.common.pojo.PageResult;
import cn.iocoder.dashboard.framework.codegen.config.CodegenProperties;
import cn.iocoder.dashboard.modules.tool.controller.codegen.vo.ToolCodegenUpdateReqVO;
import cn.iocoder.dashboard.modules.tool.controller.codegen.vo.table.ToolCodegenTablePageReqVO;
import cn.iocoder.dashboard.modules.tool.convert.codegen.ToolCodegenConvert;
import cn.iocoder.dashboard.modules.tool.dal.dataobject.codegen.ToolCodegenColumnDO;
import cn.iocoder.dashboard.modules.tool.dal.dataobject.codegen.ToolCodegenTableDO;
import cn.iocoder.dashboard.modules.tool.dal.dataobject.codegen.ToolSchemaColumnDO;
import cn.iocoder.dashboard.modules.tool.dal.dataobject.codegen.ToolSchemaTableDO;
import cn.iocoder.dashboard.modules.tool.dal.mysql.coegen.ToolCodegenColumnMapper;
import cn.iocoder.dashboard.modules.tool.dal.mysql.coegen.ToolCodegenTableMapper;
import cn.iocoder.dashboard.modules.tool.dal.mysql.coegen.ToolSchemaColumnMapper;
import cn.iocoder.dashboard.modules.tool.dal.mysql.coegen.ToolSchemaTableMapper;
import cn.iocoder.dashboard.modules.tool.service.codegen.ToolCodegenService;
import cn.iocoder.dashboard.util.collection.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 代码生成 Service 实现类
 *
 * @author 芋道源码
 */
@Service
public class ToolCodegenServiceImpl implements ToolCodegenService {

    @Resource
    private ToolSchemaTableMapper schemaTableMapper;
    @Resource
    private ToolSchemaColumnMapper schemaColumnMapper;
    @Resource
    private ToolCodegenTableMapper codegenTableMapper;
    @Resource
    private ToolCodegenColumnMapper codegenColumnMapper;

    @Resource
    private ToolCodegenBuilder codegenBuilder;
    @Resource
    private ToolCodegenEngine codegenEngine;

    @Resource
    private CodegenProperties codegenProperties;

    @Override
    @Transactional
    public Long createCodegen(String tableName) {
        // 从数据库中，获得数据库表结构
        ToolSchemaTableDO schemaTable = schemaTableMapper.selectByTableName(tableName);
        if (schemaTable == null) {
            throw new RuntimeException(""); // TODO
        }
        List<ToolSchemaColumnDO> schemaColumns = schemaColumnMapper.selectListByTableName(tableName);
        if (CollUtil.isEmpty(schemaColumns)) {
            throw new RuntimeException(""); // TODO
        }
        // 校验是否已经存在
        if (codegenTableMapper.selectByTableName(tableName) != null) {
            throw new RuntimeException(""); // TODO
        }

        // 构建 ToolCodegenTableDO 对象，插入到 DB 中
        ToolCodegenTableDO table = codegenBuilder.buildTable(schemaTable);
        codegenTableMapper.insert(table);
        // 构建 ToolCodegenColumnDO 数组，插入到 DB 中
        List<ToolCodegenColumnDO> columns = codegenBuilder.buildColumns(schemaColumns);
        columns.forEach(column -> {
            column.setTableId(table.getId());
            codegenColumnMapper.insert(column); // TODO 批量插入
        });
        return table.getId();
    }

    @Override
    @Transactional
    public List<Long> createCodeGenList(List<String> tableNames) {
        List<Long> ids = new ArrayList<>(tableNames.size());
        // 遍历添加。虽然效率会低一点，但是没必要做成完全批量，因为不会这么大量
        tableNames.forEach(tableName -> ids.add(createCodegen(tableName)));
        return ids;
    }

    @Override
    @Transactional
    public void updateCodegen(ToolCodegenUpdateReqVO updateReqVO) {
        // 校验是否已经存在
        if (codegenTableMapper.selectById(updateReqVO.getTable().getId()) == null) {
            throw new RuntimeException(""); // TODO
        }

        // 更新 table 表定义
        ToolCodegenTableDO updateTableObj = ToolCodegenConvert.INSTANCE.convert(updateReqVO.getTable());
        codegenTableMapper.updateById(updateTableObj);
        // 更新 column 字段定义
        List<ToolCodegenColumnDO> updateColumnObjs = ToolCodegenConvert.INSTANCE.convertList03(updateReqVO.getColumns());
        updateColumnObjs.forEach(updateColumnObj -> codegenColumnMapper.updateById(updateColumnObj));
    }

    @Override
    public void syncCodegen(Long tableId) {
        // 校验是否已经存在
        ToolCodegenTableDO table = codegenTableMapper.selectById(tableId);
        if (table == null) {
            throw new RuntimeException(""); // TODO
        }
        // 从数据库中，获得数据库表结构
        List<ToolSchemaColumnDO> schemaColumns = schemaColumnMapper.selectListByTableName(table.getTableName());
        if (CollUtil.isEmpty(schemaColumns)) {
            throw new RuntimeException(""); // TODO
        }
        Set<String> schemaColumnNames = CollectionUtils.convertSet(schemaColumns, ToolSchemaColumnDO::getColumnName);

        // 构建 ToolCodegenColumnDO 数组，只同步新增的字段
        List<ToolCodegenColumnDO> codegenColumns = codegenColumnMapper.selectListByTableId(tableId);
        Set<String> codegenColumnNames = CollectionUtils.convertSet(codegenColumns, ToolCodegenColumnDO::getColumnName);
        // 移除已经存在的字段
        schemaColumns.removeIf(column -> codegenColumnNames.contains(column.getColumnName()));
        // 计算需要删除的字段
        Set<Long> deleteColumnIds = codegenColumns.stream().filter(column -> !schemaColumnNames.contains(column.getColumnName()))
                .map(ToolCodegenColumnDO::getId).collect(Collectors.toSet());
        if (CollUtil.isEmpty(schemaColumns) && CollUtil.isEmpty(deleteColumnIds)) {
            throw new RuntimeException(""); // TODO
        }

        // 插入新增的字段
        List<ToolCodegenColumnDO> columns = codegenBuilder.buildColumns(schemaColumns);
        columns.forEach(column -> {
            column.setTableId(table.getId());
            codegenColumnMapper.insert(column); // TODO 批量插入
        });
        // 删除不存在的字段
        codegenColumnMapper.deleteBatchIds(deleteColumnIds);
    }

    @Override
    @Transactional
    public void deleteCodegen(Long tableId) {
        // 校验是否已经存在
        if (codegenTableMapper.selectById(tableId) == null) {
            throw new RuntimeException(""); // TODO
        }

        // 删除 table 表定义
        codegenTableMapper.deleteById(tableId);
        // 删除 column 字段定义
        codegenColumnMapper.deleteListByTableId(tableId);
    }

    @Override
    public PageResult<ToolCodegenTableDO> getCodegenTablePage(ToolCodegenTablePageReqVO pageReqVO) {
        return codegenTableMapper.selectPage(pageReqVO);
    }

    @Override
    public ToolCodegenTableDO getCodegenTablePage(Long id) {
        return codegenTableMapper.selectById(id);
    }

    @Override
    public List<ToolCodegenTableDO> getCodeGenTableList() {
        return codegenTableMapper.selectList();
    }

    @Override
    public List<ToolCodegenColumnDO> getCodegenColumnListByTableId(Long tableId) {
        return codegenColumnMapper.selectListByTableId(tableId);
    }

    @Override
    public Map<String, String> generationCodes(Long tableId) {
        // 校验是否已经存在
        ToolCodegenTableDO table = codegenTableMapper.selectById(tableId);
        if (codegenTableMapper.selectById(tableId) == null) {
            throw new RuntimeException(""); // TODO
        }
        List<ToolCodegenColumnDO> columns = codegenColumnMapper.selectListByTableId(tableId);
        if (CollUtil.isEmpty(columns)) {
            throw new RuntimeException(""); // TODO
        }

        // 执行生成
        return codegenEngine.execute(table, columns);
    }

    @Override
    public List<ToolSchemaTableDO> getSchemaTableList(String tableName, String tableComment) {
        return schemaTableMapper.selectList(codegenProperties.getDbSchemas(), tableName, tableComment);
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
