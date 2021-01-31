package cn.iocoder.dashboard.modules.tool.service.codegen.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.dashboard.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.dashboard.modules.tool.convert.codegen.CodegenConvert;
import cn.iocoder.dashboard.modules.tool.dal.mysql.dao.coegen.ToolCodegenColumnMapper;
import cn.iocoder.dashboard.modules.tool.dal.mysql.dao.coegen.ToolCodegenTableMapper;
import cn.iocoder.dashboard.modules.tool.dal.mysql.dao.coegen.ToolInformationSchemaColumnMapper;
import cn.iocoder.dashboard.modules.tool.dal.mysql.dao.coegen.ToolInformationSchemaTableMapper;
import cn.iocoder.dashboard.modules.tool.dal.mysql.dataobject.codegen.ToolCodegenColumnDO;
import cn.iocoder.dashboard.modules.tool.dal.mysql.dataobject.codegen.ToolCodegenTableDO;
import cn.iocoder.dashboard.modules.tool.dal.mysql.dataobject.codegen.ToolInformationSchemaColumnDO;
import cn.iocoder.dashboard.modules.tool.dal.mysql.dataobject.codegen.ToolInformationSchemaTableDO;
import cn.iocoder.dashboard.modules.tool.enums.codegen.ToolCodegenColumnHtmlTypeEnum;
import cn.iocoder.dashboard.modules.tool.enums.codegen.ToolCodegenColumnListConditionEnum;
import cn.iocoder.dashboard.modules.tool.service.codegen.ToolCodegenService;
import com.google.common.collect.Sets;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;

/**
 * 代码生成 Service 实现类
 *
 * @author 芋道源码
 */
@Service
public class ToolCodegenServiceImpl implements ToolCodegenService {

    /**
     * 字段名与 {@link ToolCodegenColumnListConditionEnum} 的默认映射
     * 注意，字段的匹配以后缀的方式
     */
    private static final Map<String, ToolCodegenColumnListConditionEnum> columnListOperationConditionMappings =
            MapUtil.<String, ToolCodegenColumnListConditionEnum>builder()
                    .put("name", ToolCodegenColumnListConditionEnum.LIKE)
                    .put("time", ToolCodegenColumnListConditionEnum.BETWEEN)
                    .put("date", ToolCodegenColumnListConditionEnum.BETWEEN)
                    .build();

    /**
     * 字段名与 {@link ToolCodegenColumnHtmlTypeEnum} 的默认映射
     * 注意，字段的匹配以后缀的方式
     */
    private static final Map<String, ToolCodegenColumnHtmlTypeEnum> columnHtmlTypeMappings =
            MapUtil.<String, ToolCodegenColumnHtmlTypeEnum>builder()
                    .put("status", ToolCodegenColumnHtmlTypeEnum.RADIO)
                    .put("sex", ToolCodegenColumnHtmlTypeEnum.RADIO)
                    .put("type", ToolCodegenColumnHtmlTypeEnum.SELECT)
                    .put("image", ToolCodegenColumnHtmlTypeEnum.UPLOAD_IMAGE)
                    .put("file", ToolCodegenColumnHtmlTypeEnum.UPLOAD_FILE)
                    .put("content", ToolCodegenColumnHtmlTypeEnum.EDITOR)
                    .build();

    /**
     * 新增操作，不需要传递的字段
     */
    private static final Set<String> CREATE_OPERATION_EXCLUDE_COLUMN = Sets.newHashSet(
            "id");
    /**
     * 修改操作，不需要传递的字段
     */
    private static final Set<String> UPDATE_OPERATION_EXCLUDE_COLUMN = Sets.newHashSet();
    /**
     * 列表操作的条件，不需要传递的字段
     */
    private static final Set<String> LIST_OPERATION_CONDITION_COLUMN = Sets.newHashSet();
    /**
     * 列表操作的结果，不需要返回的字段
     */
    private static final Set<String> LIST_OPERATION_RESULT_COLUMN = Sets.newHashSet();

    /**
     * Java 类型与 MySQL 类型的映射关系
     */
    private static final Map<String, Set<String>> javaTypeMappings = MapUtil.<String, Set<String>>builder()
            .put(Boolean.class.getSimpleName(), Sets.newHashSet("bit"))
            .put(Integer.class.getSimpleName(), Sets.newHashSet("tinyint", "smallint", "mediumint", "int"))
            .put(Long.class.getSimpleName(), Collections.singleton("bigint"))
            .put(Double.class.getSimpleName(), Sets.newHashSet("float", "double"))
            .put(BigDecimal.class.getSimpleName(), Sets.newHashSet("decimal", "numeric"))
            .put(String.class.getSimpleName(), Sets.newHashSet("tinytext", "text", "mediumtext", "longtext", // 长文本
                    "char", "varchar", "nvarchar", "varchar2")) // 短文本
            .put(Date.class.getSimpleName(), Sets.newHashSet("datetime", "time", "date", "timestamp"))
            .build();

    static {
        Arrays.stream(BaseDO.class.getDeclaredFields()).forEach(field -> {
            CREATE_OPERATION_EXCLUDE_COLUMN.add(field.getName());
            UPDATE_OPERATION_EXCLUDE_COLUMN.add(field.getName());
            LIST_OPERATION_CONDITION_COLUMN.add(field.getName());
            LIST_OPERATION_RESULT_COLUMN.add(field.getName());
        });
        LIST_OPERATION_RESULT_COLUMN.remove("create_time"); // 创建时间，还是需要返回的
    }

    @Resource
    private ToolInformationSchemaTableMapper informationSchemaTableMapper;
    @Resource
    private ToolInformationSchemaColumnMapper informationSchemaColumnMapper;
    @Resource
    private ToolCodegenTableMapper codegenTableMapper;
    @Resource
    private ToolCodegenColumnMapper codegenColumnMapper;

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

        // 将 table 插入到数据库
        ToolCodegenTableDO table = CodegenConvert.INSTANCE.convert(schemaTable);
        initTableDefault(table);
        codegenTableMapper.insert(table);
        // 将 column 插入到数据库
        List<ToolCodegenColumnDO> columns = CodegenConvert.INSTANCE.convertList(schemaColumns);
        columns.forEach(column -> {
            initColumnDefault(column);
            column.setTableId(table.getId());
            codegenColumnMapper.insert(column);
        });
        return table.getId();
    }

    /**
     * 初始化 Table 表的默认字段
     *
     * @param table 表定义
     */
    private void initTableDefault(ToolCodegenTableDO table) {
        table.setModuleName(StrUtil.subBefore(table.getTableName(),
                '_', false)); // 第一个 _ 前缀的前面，作为 module 名字
        table.setBusinessName(StrUtil.subAfter(table.getTableName(),
                '_', false)); // 第一个 _ 前缀的后面，作为 module 名字
        table.setBusinessName(StrUtil.toCamelCase(table.getBusinessName())); // 可能存在多个 _ 的情况，转换成驼峰
        table.setClassName(StrUtil.toCamelCase(table.getClassName())); // 驼峰
        table.setClassComment(StrUtil.subBefore(table.getClassComment(), // 去除结尾的表，作为类描述
                '表', true));
        table.setAuthor("芋艿"); // TODO 稍后改成创建人
    }

    /**
     * 初始化 Column 列的默认字段
     *
     * @param column 列定义
     */
    private void initColumnDefault(ToolCodegenColumnDO column) {
        // 处理 Java 相关的字段的默认值
        processColumnJava(column);
        // 处理 CRUD 相关的字段的默认值
        processColumnOperation(column);
        // 处理 UI 相关的字段的默认值
        processColumnUI(column);
    }

    private void processColumnJava(ToolCodegenColumnDO column) {
        // 处理 javaField 字段
        column.setJavaField(StrUtil.toCamelCase(column.getColumnName()));
        // 处理 dictType 字段，暂无
        // 处理 javaType 字段
        String dbType = StrUtil.subBefore(column.getColumnName(), ')', false);
        javaTypeMappings.entrySet().stream()
                .filter(entry -> entry.getValue().contains(dbType))
                .findFirst().ifPresent(entry -> column.setJavaType(entry.getKey()));
        if (column.getJavaType() == null) {
            throw new IllegalStateException(String.format("column(%s) 的数据库类型(%s) 找不到匹配的 Java 类型",
                    column.getColumnName(), column.getColumnType()));
        }
    }

    private void processColumnOperation(ToolCodegenColumnDO column) {
        // 处理 createOperation 字段
        column.setCreateOperation(!CREATE_OPERATION_EXCLUDE_COLUMN.contains(column.getColumnName())
            && !column.getPrimaryKey()); // 非主键
        // 处理 updateOperation 字段
        column.setUpdateOperation(!UPDATE_OPERATION_EXCLUDE_COLUMN.contains(column.getColumnName()));
        // 处理 listOperationResult 字段
        column.setListOperationResult(!LIST_OPERATION_RESULT_COLUMN.contains(column.getColumnName()));
        // 处理 listOperationCondition 字段。默认设置为需要过滤的条件，手动进行取消
        if (!LIST_OPERATION_CONDITION_COLUMN.contains(column.getColumnName())
            && !column.getPrimaryKey()) { // 非主键
            column.setListOperationCondition(ToolCodegenColumnListConditionEnum.EQ.getCondition());
        }
        columnListOperationConditionMappings.entrySet().stream()
                .filter(entry -> StrUtil.endWithIgnoreCase(column.getColumnName(), entry.getKey()))
                .findFirst().ifPresent(entry -> column.setListOperationCondition(entry.getValue().getCondition()));
    }

    private void processColumnUI(ToolCodegenColumnDO column) {
        // 基于后缀进行匹配
        columnHtmlTypeMappings.entrySet().stream()
                .filter(entry -> StrUtil.endWithIgnoreCase(column.getColumnName(), entry.getKey()))
                .findFirst().ifPresent(entry -> column.setHtmlType(entry.getValue().getType()));
        // 如果是 Boolean 类型时，设置为 radio 类型.
        // 其它类型，因为字段名可以相对保障，所以不进行处理。例如说 date 对应 datetime 类型.
        if (Boolean.class.getSimpleName().equals(column.getJavaType())) {
            column.setHtmlType(ToolCodegenColumnHtmlTypeEnum.RADIO.getType());
        }
        // 兜底，设置默认为 input 类型
        if (column.getHtmlType() == null) {
            column.setHtmlType(ToolCodegenColumnHtmlTypeEnum.INPUT.getType());
        }
    }

}
