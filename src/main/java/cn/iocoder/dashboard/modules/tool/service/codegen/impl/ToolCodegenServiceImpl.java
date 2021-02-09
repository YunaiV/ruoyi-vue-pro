package cn.iocoder.dashboard.modules.tool.service.codegen.impl;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.dashboard.common.pojo.PageResult;
import cn.iocoder.dashboard.modules.tool.controller.codegen.vo.ToolCodegenTablePageReqVO;
import cn.iocoder.dashboard.modules.tool.dal.mysql.dao.coegen.ToolCodegenColumnMapper;
import cn.iocoder.dashboard.modules.tool.dal.mysql.dao.coegen.ToolCodegenTableMapper;
import cn.iocoder.dashboard.modules.tool.dal.mysql.dao.coegen.ToolInformationSchemaColumnMapper;
import cn.iocoder.dashboard.modules.tool.dal.mysql.dao.coegen.ToolInformationSchemaTableMapper;
import cn.iocoder.dashboard.modules.tool.dal.mysql.dataobject.codegen.ToolCodegenColumnDO;
import cn.iocoder.dashboard.modules.tool.dal.mysql.dataobject.codegen.ToolCodegenTableDO;
import cn.iocoder.dashboard.modules.tool.dal.mysql.dataobject.codegen.ToolInformationSchemaColumnDO;
import cn.iocoder.dashboard.modules.tool.dal.mysql.dataobject.codegen.ToolInformationSchemaTableDO;
import cn.iocoder.dashboard.modules.tool.service.codegen.ToolCodegenService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

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
    public Long createCodegenTable(String tableName) {
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

}
