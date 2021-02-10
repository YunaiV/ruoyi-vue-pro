package cn.iocoder.dashboard.modules.tool.service.codegen.impl;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.dashboard.common.pojo.PageResult;
import cn.iocoder.dashboard.modules.tool.controller.codegen.vo.ToolCodegenUpdateReqVO;
import cn.iocoder.dashboard.modules.tool.controller.codegen.vo.table.ToolCodegenTablePageReqVO;
import cn.iocoder.dashboard.modules.tool.convert.codegen.ToolCodegenConvert;
import cn.iocoder.dashboard.modules.tool.dal.mysql.coegen.ToolCodegenColumnMapper;
import cn.iocoder.dashboard.modules.tool.dal.mysql.coegen.ToolCodegenTableMapper;
import cn.iocoder.dashboard.modules.tool.dal.mysql.coegen.ToolInformationSchemaColumnMapper;
import cn.iocoder.dashboard.modules.tool.dal.mysql.coegen.ToolInformationSchemaTableMapper;
import cn.iocoder.dashboard.modules.tool.dal.dataobject.codegen.ToolCodegenColumnDO;
import cn.iocoder.dashboard.modules.tool.dal.dataobject.codegen.ToolCodegenTableDO;
import cn.iocoder.dashboard.modules.tool.dal.dataobject.codegen.ToolInformationSchemaColumnDO;
import cn.iocoder.dashboard.modules.tool.dal.dataobject.codegen.ToolInformationSchemaTableDO;
import cn.iocoder.dashboard.modules.tool.service.codegen.ToolCodegenService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * 代码生成 Service 实现类
 *
 * @author 芋道源码
 */
@Service
public class ToolCodegenServiceImpl implements ToolCodegenService {

    @Resource
    private ToolInformationSchemaTableMapper informationSchemaTableMapper;
    @Resource
    private ToolInformationSchemaColumnMapper informationSchemaColumnMapper;
    @Resource
    private ToolCodegenTableMapper codegenTableMapper;
    @Resource
    private ToolCodegenColumnMapper codegenColumnMapper;

    @Resource
    private ToolCodegenBuilder codegenBuilder;
    @Resource
    private ToolCodegenEngine codegenEngine;

    @Override
    @Transactional
    public Long createCodegen(String tableName) {
        // 从数据库中，获得数据库表结构
        ToolInformationSchemaTableDO schemaTable = informationSchemaTableMapper.selectByTableName(tableName);
        if (schemaTable == null) {
            throw new RuntimeException(""); // TODO
        }
        List<ToolInformationSchemaColumnDO> schemaColumns = informationSchemaColumnMapper.selectListByTableName(tableName);
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
    public PageResult<ToolCodegenTableDO> getCodeGenTablePage(ToolCodegenTablePageReqVO pageReqVO) {
        return codegenTableMapper.selectPage(pageReqVO);
    }

    @Override
    public ToolCodegenTableDO getCodeGenTablePage(Long id) {
        return codegenTableMapper.selectById(id);
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

}
