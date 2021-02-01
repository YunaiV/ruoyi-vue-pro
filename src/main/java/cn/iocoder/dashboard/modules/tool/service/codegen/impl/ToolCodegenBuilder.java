package cn.iocoder.dashboard.modules.tool.service.codegen.impl;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.dashboard.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.dashboard.modules.tool.convert.codegen.CodegenConvert;
import cn.iocoder.dashboard.modules.tool.dal.mysql.dataobject.codegen.ToolCodegenColumnDO;
import cn.iocoder.dashboard.modules.tool.dal.mysql.dataobject.codegen.ToolCodegenTableDO;
import cn.iocoder.dashboard.modules.tool.dal.mysql.dataobject.codegen.ToolInformationSchemaColumnDO;
import cn.iocoder.dashboard.modules.tool.dal.mysql.dataobject.codegen.ToolInformationSchemaTableDO;
import cn.iocoder.dashboard.modules.tool.enums.codegen.ToolCodegenColumnHtmlTypeEnum;
import cn.iocoder.dashboard.modules.tool.enums.codegen.ToolCodegenColumnListConditionEnum;
import cn.iocoder.dashboard.modules.tool.enums.codegen.ToolCodegenTemplateTypeEnum;
import com.google.common.collect.Sets;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;

/**
 * 代码生成器的 Builder，负责：
 * 1. 将数据库的表 {@link ToolInformationSchemaTableDO} 定义，构建成 {@link ToolCodegenTableDO}
 * 2. 将数据库的列 {@link ToolInformationSchemaColumnDO} 构定义，建成 {@link ToolCodegenColumnDO}
 */
@Component
public class ToolCodegenBuilder {

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
     * {@link BaseDO} 的字段
     */
    public static final Set<String> BASE_DO_FIELDS = new HashSet<>();
    /**
     * 新增操作，不需要传递的字段
     */
    private static final Set<String> CREATE_OPERATION_EXCLUDE_COLUMN = Sets.newHashSet("id");
    /**
     * 修改操作，不需要传递的字段
     */
    private static final Set<String> UPDATE_OPERATION_EXCLUDE_COLUMN = Sets.newHashSet();
    /**
     * 列表操作的条件，不需要传递的字段
     */
    private static final Set<String> LIST_OPERATION_EXCLUDE_COLUMN = Sets.newHashSet("id");
    /**
     * 列表操作的结果，不需要返回的字段
     */
    private static final Set<String> LIST_OPERATION_RESULT_EXCLUDE_COLUMN = Sets.newHashSet();

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
        Arrays.stream(BaseDO.class.getDeclaredFields()).forEach(field -> BASE_DO_FIELDS.add(field.getName()));
        // 处理 OPERATION 相关的字段
        CREATE_OPERATION_EXCLUDE_COLUMN.addAll(BASE_DO_FIELDS);
        UPDATE_OPERATION_EXCLUDE_COLUMN.addAll(BASE_DO_FIELDS);
        LIST_OPERATION_EXCLUDE_COLUMN.addAll(BASE_DO_FIELDS);
        LIST_OPERATION_EXCLUDE_COLUMN.remove("create_time"); // 创建时间，还是可能需要传递的
        LIST_OPERATION_RESULT_EXCLUDE_COLUMN.addAll(BASE_DO_FIELDS);
        LIST_OPERATION_RESULT_EXCLUDE_COLUMN.remove("create_time"); // 创建时间，还是需要返回的
    }

    public ToolCodegenTableDO buildTable(ToolInformationSchemaTableDO schemaTable) {
        ToolCodegenTableDO table = CodegenConvert.INSTANCE.convert(schemaTable);
        initTableDefault(table);
        return table;
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
        table.setClassName(StrUtil.upperFirst(StrUtil.toCamelCase(table.getTableName()))); // 驼峰 + 首字母大写
        table.setClassComment(StrUtil.subBefore(table.getTableComment(), // 去除结尾的表，作为类描述
                '表', true));
        table.setAuthor("芋艿"); // TODO 稍后改成创建人
        table.setTemplateType(ToolCodegenTemplateTypeEnum.CRUD.getType());
    }

    public List<ToolCodegenColumnDO> buildColumns(List<ToolInformationSchemaColumnDO> schemaColumns) {
        List<ToolCodegenColumnDO> columns = CodegenConvert.INSTANCE.convertList(schemaColumns);
        columns.forEach(this::initColumnDefault);
        return columns;
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
        String dbType = StrUtil.subBefore(column.getColumnType(), '(', false);
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
        column.setCreateOperation(!CREATE_OPERATION_EXCLUDE_COLUMN.contains(column.getJavaField())
                && !column.getPrimaryKey()); // 对于主键，创建时无需传递
        // 处理 updateOperation 字段
        column.setUpdateOperation(!UPDATE_OPERATION_EXCLUDE_COLUMN.contains(column.getJavaField())
                || column.getPrimaryKey()); // 对于主键，更新时需要传递
        // 处理 listOperation 字段
        column.setListOperation(!LIST_OPERATION_EXCLUDE_COLUMN.contains(column.getJavaField())
                && !column.getPrimaryKey()); // 对于主键，列表过滤不需要传递
        // 处理 listOperationCondition 字段
        columnListOperationConditionMappings.entrySet().stream()
                .filter(entry -> StrUtil.endWithIgnoreCase(column.getJavaField(), entry.getKey()))
                .findFirst().ifPresent(entry -> column.setListOperationCondition(entry.getValue().getCondition()));
        if (column.getListOperationCondition() == null) {
            column.setListOperationCondition(ToolCodegenColumnListConditionEnum.EQ.getCondition());
        }
        // 处理 listOperationResult 字段
        column.setListOperationResult(!LIST_OPERATION_RESULT_EXCLUDE_COLUMN.contains(column.getJavaField()));
    }

    private void processColumnUI(ToolCodegenColumnDO column) {
        // 基于后缀进行匹配
        columnHtmlTypeMappings.entrySet().stream()
                .filter(entry -> StrUtil.endWithIgnoreCase(column.getJavaField(), entry.getKey()))
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
