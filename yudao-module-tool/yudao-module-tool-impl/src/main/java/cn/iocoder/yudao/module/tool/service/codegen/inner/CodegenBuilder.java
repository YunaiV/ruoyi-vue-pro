package cn.iocoder.yudao.module.tool.service.codegen.inner;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.tool.convert.codegen.CodegenConvert;
import cn.iocoder.yudao.module.tool.dal.dataobject.codegen.CodegenColumnDO;
import cn.iocoder.yudao.module.tool.dal.dataobject.codegen.CodegenTableDO;
import cn.iocoder.yudao.module.tool.dal.dataobject.codegen.SchemaColumnDO;
import cn.iocoder.yudao.module.tool.dal.dataobject.codegen.SchemaTableDO;
import cn.iocoder.yudao.module.tool.enums.codegen.CodegenColumnHtmlTypeEnum;
import cn.iocoder.yudao.module.tool.enums.codegen.CodegenColumnListConditionEnum;
import cn.iocoder.yudao.module.tool.enums.codegen.CodegenTemplateTypeEnum;
import com.google.common.collect.Sets;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;

import static cn.hutool.core.text.CharSequenceUtil.*;

/**
 * 代码生成器的 Builder，负责：
 * 1. 将数据库的表 {@link SchemaTableDO} 定义，构建成 {@link CodegenTableDO}
 * 2. 将数据库的列 {@link SchemaColumnDO} 构定义，建成 {@link CodegenColumnDO}
 */
@Component
public class CodegenBuilder {

    /**
     * Module 名字的映射 TODO 后续梳理到配置类
     *
     * key：模块的完整名
     * value：模块的缩写名
     */
    private static final Map<String, String> moduleNames = MapUtil.<String, String>builder()
            .put("system", "sys")
            .put("infra", "inf")
            .put("tool", "tool")
            .build();

    /**
     * 字段名与 {@link CodegenColumnListConditionEnum} 的默认映射
     * 注意，字段的匹配以后缀的方式
     */
    private static final Map<String, CodegenColumnListConditionEnum> columnListOperationConditionMappings =
            MapUtil.<String, CodegenColumnListConditionEnum>builder()
                    .put("name", CodegenColumnListConditionEnum.LIKE)
                    .put("time", CodegenColumnListConditionEnum.BETWEEN)
                    .put("date", CodegenColumnListConditionEnum.BETWEEN)
                    .build();

    /**
     * 字段名与 {@link CodegenColumnHtmlTypeEnum} 的默认映射
     * 注意，字段的匹配以后缀的方式
     */
    private static final Map<String, CodegenColumnHtmlTypeEnum> columnHtmlTypeMappings =
            MapUtil.<String, CodegenColumnHtmlTypeEnum>builder()
                    .put("status", CodegenColumnHtmlTypeEnum.RADIO)
                    .put("sex", CodegenColumnHtmlTypeEnum.RADIO)
                    .put("type", CodegenColumnHtmlTypeEnum.SELECT)
                    .put("image", CodegenColumnHtmlTypeEnum.UPLOAD_IMAGE)
                    .put("file", CodegenColumnHtmlTypeEnum.UPLOAD_FILE)
                    .put("content", CodegenColumnHtmlTypeEnum.EDITOR)
                    .put("time", CodegenColumnHtmlTypeEnum.DATETIME)
                    .put("date", CodegenColumnHtmlTypeEnum.DATETIME)
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
            .put("byte[]", Sets.newHashSet("blob"))
            .build();

    static {
        Arrays.stream(BaseDO.class.getDeclaredFields()).forEach(field -> BASE_DO_FIELDS.add(field.getName()));
        // 处理 OPERATION 相关的字段
        CREATE_OPERATION_EXCLUDE_COLUMN.addAll(BASE_DO_FIELDS);
        UPDATE_OPERATION_EXCLUDE_COLUMN.addAll(BASE_DO_FIELDS);
        LIST_OPERATION_EXCLUDE_COLUMN.addAll(BASE_DO_FIELDS);
        LIST_OPERATION_EXCLUDE_COLUMN.remove("createTime"); // 创建时间，还是可能需要传递的
        LIST_OPERATION_RESULT_EXCLUDE_COLUMN.addAll(BASE_DO_FIELDS);
        LIST_OPERATION_RESULT_EXCLUDE_COLUMN.remove("createTime"); // 创建时间，还是需要返回的
    }

    public CodegenTableDO buildTable(SchemaTableDO schemaTable) {
        CodegenTableDO table = CodegenConvert.INSTANCE.convert(schemaTable);
        initTableDefault(table);
        return table;
    }

    /**
     * 初始化 Table 表的默认字段
     *
     * @param table 表定义
     */
    private void initTableDefault(CodegenTableDO table) {
        table.setModuleName(getFullModuleName(StrUtil.subBefore(table.getTableName(),
                '_', false))); // 第一个 _ 前缀的前面，作为 module 名字
        table.setBusinessName(toCamelCase(subAfter(table.getTableName(),
                '_', false))); // 第一步，第一个 _ 前缀的后面，作为 module 名字; 第二步，可能存在多个 _ 的情况，转换成驼峰
        table.setClassName(upperFirst(toCamelCase(table.getTableName()))); // 驼峰 + 首字母大写
        table.setClassComment(subBefore(table.getTableComment(), // 去除结尾的表，作为类描述
                '表', true));
        table.setAuthor("芋艿"); // TODO 稍后改成创建人
        table.setTemplateType(CodegenTemplateTypeEnum.CRUD.getType());
    }

    public List<CodegenColumnDO> buildColumns(List<SchemaColumnDO> schemaColumns) {
        List<CodegenColumnDO> columns = CodegenConvert.INSTANCE.convertList(schemaColumns);
        columns.forEach(this::initColumnDefault);
        return columns;
    }

    /**
     * 初始化 Column 列的默认字段
     *
     * @param column 列定义
     */
    private void initColumnDefault(CodegenColumnDO column) {
        // 处理 Java 相关的字段的默认值
        processColumnJava(column);
        // 处理 CRUD 相关的字段的默认值
        processColumnOperation(column);
        // 处理 UI 相关的字段的默认值
        processColumnUI(column);
    }

    private void processColumnJava(CodegenColumnDO column) {
        // 处理 javaField 字段
        column.setJavaField(toCamelCase(column.getColumnName()));
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

    private void processColumnOperation(CodegenColumnDO column) {
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
            column.setListOperationCondition(CodegenColumnListConditionEnum.EQ.getCondition());
        }
        // 处理 listOperationResult 字段
        column.setListOperationResult(!LIST_OPERATION_RESULT_EXCLUDE_COLUMN.contains(column.getJavaField()));
    }

    private void processColumnUI(CodegenColumnDO column) {
        // 基于后缀进行匹配
        columnHtmlTypeMappings.entrySet().stream()
                .filter(entry -> StrUtil.endWithIgnoreCase(column.getJavaField(), entry.getKey()))
                .findFirst().ifPresent(entry -> column.setHtmlType(entry.getValue().getType()));
        // 如果是 Boolean 类型时，设置为 radio 类型.
        // 其它类型，因为字段名可以相对保障，所以不进行处理。例如说 date 对应 datetime 类型.
        if (Boolean.class.getSimpleName().equals(column.getJavaType())) {
            column.setHtmlType(CodegenColumnHtmlTypeEnum.RADIO.getType());
        }
        // 兜底，设置默认为 input 类型
        if (column.getHtmlType() == null) {
            column.setHtmlType(CodegenColumnHtmlTypeEnum.INPUT.getType());
        }
    }

    /**
     * 获得模块的缩略名
     *
     * @param fullModuleName 模块的完整名
     * @return 缩略名
     */
    public String getSimpleModuleName(String fullModuleName) {
        return moduleNames.getOrDefault(fullModuleName, fullModuleName);
    }

    /**
     * 获得模块的完整名
     *
     * @param shortModuleName 模块的缩略名
     * @return 完整名
     */
    public String getFullModuleName(String shortModuleName) {
        return moduleNames.entrySet().stream()
                .filter(entry -> entry.getValue().equals(shortModuleName)) // 匹配
                .findFirst().map(Map.Entry::getKey) // 返回 key
                .orElse(shortModuleName); // 兜底返回 shortModuleName
    }

}
