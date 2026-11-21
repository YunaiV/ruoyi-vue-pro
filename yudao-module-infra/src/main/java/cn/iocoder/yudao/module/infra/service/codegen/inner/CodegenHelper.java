package cn.iocoder.yudao.module.infra.service.codegen.inner;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.excel.core.convert.JsonConvert;
import cn.iocoder.yudao.framework.excel.core.convert.MultiDictConvert;
import cn.iocoder.yudao.module.infra.dal.dataobject.codegen.CodegenColumnDO;
import cn.iocoder.yudao.module.infra.enums.codegen.CodegenColumnHtmlTypeEnum;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.anyMatch;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.filterList;

/**
 * 代码生成的模板 Helper
 *
 * 由 {@link CodegenEngine} 创建实例，绑定到 vm 模板的 codegenHelper 变量，用于承载模板中的通用判断逻辑，
 * 避免各个 vm 模板分别判断，导致后端、前端生成的代码不一致。
 *
 * 目前包含：
 * 一、【集合（多选）字段】的逻辑，规则如下：
 *   1. htmlType 为 checkbox 复选框，javaType 为 String、Integer、Long
 *   2. javaType 保留元素类型，生成代码时派生为 List&lt;T&gt;
 *   3. 数据库使用 varchar / text 存储 JSON 数组字符串（例如 ["a","b"]、[1,2]），通过 MyBatis Plus 的 JacksonTypeHandler 转换
 * 二、【表单：组件按需导入】的逻辑：判断表单使用到的组件、必填校验等，避免生成未使用的 import
 *
 * @author 芋道源码
 */
public class CodegenHelper {

    /**
     * Excel 转换器的使用场景：Response VO 导出（listOperationResult 字段）
     */
    public static final String EXCEL_SCENE_RESP = "resp";
    /**
     * Excel 转换器的使用场景：Excel 导入 VO（createOperation 字段）
     */
    public static final String EXCEL_SCENE_IMPORT = "import";

    // ==================== 集合字段：判断 ====================

    /**
     * 判断字段是否为集合（多选）字段
     *
     * @param column 字段
     * @return 是否集合字段
     */
    public boolean isCollection(CodegenColumnDO column) {
        return CodegenColumnHtmlTypeEnum.CHECKBOX.getType().equals(column.getHtmlType())
                && StrUtil.equalsAny(column.getJavaType(), "String", "Integer", "Long");
    }

    /**
     * 判断字段是否为 Long 元素的集合字段
     *
     * 前端使用字符串表示 Long 元素（字典选项、表单回显），避免超过 JavaScript 安全整数（2^53 - 1）后精度丢失；
     * 后端 Jackson 反序列化时，会将字符串转换为 Long，序列化时也会将超过安全范围的 Long 输出为字符串
     *
     * @param column 字段
     * @return 是否 Long 元素的集合字段
     */
    public boolean isLongCollection(CodegenColumnDO column) {
        return isCollection(column) && "Long".equals(column.getJavaType());
    }

    /**
     * 判断字段数组中，是否存在集合字段
     *
     * 用于 DO 判断是否开启 autoResultMap 等
     *
     * @param columns 字段数组
     * @return 是否存在
     */
    public boolean hasCollection(Collection<CodegenColumnDO> columns) {
        return anyMatch(columns, this::isCollection);
    }

    /**
     * 判断字段数组中，是否存在作为查询条件的集合字段
     *
     * @param columns 字段数组
     * @return 是否存在
     */
    public boolean hasCollectionListCondition(Collection<CodegenColumnDO> columns) {
        return anyMatch(columns, column -> BooleanUtil.isTrue(column.getListOperation()) && isCollection(column));
    }

    /**
     * 判断字段数组中，是否存在使用字典、并且在列表中展示的集合字段
     *
     * 用于前端判断是否需要引入 getDictLabel 等
     *
     * @param columns 字段数组
     * @return 是否存在
     */
    public boolean hasCollectionDictListResult(Collection<CodegenColumnDO> columns) {
        return anyMatch(columns, column -> BooleanUtil.isTrue(column.getListOperationResult())
                && StrUtil.isNotEmpty(column.getDictType()) && isCollection(column));
    }

    /**
     * 判断多个子表的字段数组中，是否存在使用字典、并且在列表中展示的集合字段
     *
     * @param subColumnsList 子表的字段数组的数组
     * @return 是否存在
     */
    public boolean hasSubCollectionDictListResult(Collection<? extends Collection<CodegenColumnDO>> subColumnsList) {
        return CollUtil.isNotEmpty(subColumnsList) && anyMatch(subColumnsList, this::hasCollectionDictListResult);
    }

    // ==================== 集合字段：表单 ====================

    /**
     * 获得表单（新增或修改）中的集合字段
     *
     * 用于前端表单回显时，将历史数据的 null 归一化为空数组等
     *
     * @param columns 字段数组
     * @return 集合字段数组
     */
    public List<CodegenColumnDO> getCollectionFormColumns(Collection<CodegenColumnDO> columns) {
        return filterList(columns, this::isCollectionFormColumn);
    }

    /**
     * 判断字段数组中，是否存在表单（新增或修改）中的集合字段
     *
     * @param columns 字段数组
     * @return 是否存在
     */
    public boolean hasCollectionFormColumn(Collection<CodegenColumnDO> columns) {
        return anyMatch(columns, this::isCollectionFormColumn);
    }

    /**
     * 判断多个子表的字段数组中，是否存在表单（新增或修改）中的集合字段
     *
     * @param subColumnsList 子表的字段数组的数组
     * @return 是否存在
     */
    public boolean hasSubCollectionFormColumn(Collection<? extends Collection<CodegenColumnDO>> subColumnsList) {
        return CollUtil.isNotEmpty(subColumnsList) && anyMatch(subColumnsList, this::hasCollectionFormColumn);
    }

    private boolean isCollectionFormColumn(CodegenColumnDO column) {
        return isFormColumn(column) && isCollection(column);
    }

    // ==================== 集合字段：类型 ====================

    /**
     * 获得字段在 DO、Save/Resp/Import VO 中的 Java 类型
     *
     * 集合字段返回 List&lt;元素类型&gt;，其它字段返回 javaType 本身
     *
     * @param column 字段
     * @return Java 类型
     */
    public String getJavaType(CodegenColumnDO column) {
        return isCollection(column) ? "List<" + column.getJavaType() + ">" : column.getJavaType();
    }

    /**
     * 获得集合字段在 TypeScript 中的类型
     *
     * 注意：Long 元素在前端使用字符串表示，但是接口返回的未超过安全范围的 Long 仍然是 number，所以使用 (number | string)[]，
     * 参见 {@link #isLongCollection(CodegenColumnDO)} 方法
     *
     * @param column 字段
     * @return TypeScript 类型，例如说 string[]、number[]、(number | string)[]
     */
    public String getCollectionTsType(CodegenColumnDO column) {
        if ("String".equals(column.getJavaType())) {
            return "string[]";
        }
        return isLongCollection(column) ? "(number | string)[]" : "number[]";
    }

    // ==================== 集合字段：Excel ====================

    /**
     * 获得集合字段的 Excel 转换器类名（简单类名）
     *
     * 1. 设置数据字典：{@link MultiDictConvert}，Excel 中使用“、”分隔的字典标签，支持导入、导出
     * 2. 未设置数据字典：{@link JsonConvert}，Excel 中使用 JSON 数组，仅支持导出
     *
     * @param column 字段
     * @return 转换器类名；非集合字段返回 null
     */
    public String getExcelConverter(CodegenColumnDO column) {
        Class<?> converterClass = getExcelConverterClass(column);
        return converterClass != null ? converterClass.getSimpleName() : null;
    }

    /**
     * 获得集合字段需要 import 的 Excel 转换器类名（全类名）
     *
     * @param columns 字段数组
     * @param scene   使用场景，参见 {@link #EXCEL_SCENE_RESP}、{@link #EXCEL_SCENE_IMPORT}；其它值表示全部字段
     * @return 转换器全类名数组，已去重、排序
     */
    public List<String> getExcelConverterClassNames(Collection<CodegenColumnDO> columns, String scene) {
        List<CodegenColumnDO> sceneColumns = filterList(columns, column -> isExcelSceneColumn(column, scene));
        Set<Class<?>> converterClasses = convertSet(sceneColumns, this::getExcelConverterClass); // 去重，并忽略 null（非集合字段）
        List<String> classNames = convertList(converterClasses, Class::getName); // 显式声明类型，避免嵌套调用时泛型推断为 Object
        return CollUtil.sort(classNames, Comparator.naturalOrder());
    }

    private boolean isExcelSceneColumn(CodegenColumnDO column, String scene) {
        if (EXCEL_SCENE_RESP.equals(scene)) {
            return BooleanUtil.isTrue(column.getListOperationResult());
        }
        if (EXCEL_SCENE_IMPORT.equals(scene)) {
            return BooleanUtil.isTrue(column.getCreateOperation());
        }
        return true;
    }

    private Class<?> getExcelConverterClass(CodegenColumnDO column) {
        if (!isCollection(column)) {
            return null;
        }
        return StrUtil.isNotEmpty(column.getDictType()) ? MultiDictConvert.class : JsonConvert.class;
    }

    // ==================== 表单：组件按需导入 ====================

    /**
     * 判断表单（新增或修改）中，是否存在指定 htmlType 的字段
     *
     * 用于前端按需 import 表单组件，避免生成未使用的 import，导致 TypeScript noUnusedLocals 校验失败。
     * 注意：主键不会渲染为 input 文本框，所以不计入
     *
     * @param columns  字段数组
     * @param htmlType 显示类型，参见 {@link CodegenColumnHtmlTypeEnum}
     * @return 是否存在
     */
    public boolean hasFormHtmlType(Collection<CodegenColumnDO> columns, String htmlType) {
        return anyMatch(columns, column -> isFormColumn(column) && StrUtil.equals(column.getHtmlType(), htmlType)
                && !(BooleanUtil.isTrue(column.getPrimaryKey()) && CodegenColumnHtmlTypeEnum.INPUT.getType().equals(htmlType)));
    }

    /**
     * 判断表单（新增或修改）中，是否存在使用数据字典的 select、radio、checkbox 字段
     *
     * 用于前端按需 import DICT_TYPE、getDictOptions
     *
     * @param columns 字段数组
     * @return 是否存在
     */
    public boolean hasFormDict(Collection<CodegenColumnDO> columns) {
        return anyMatch(columns, column -> isFormColumn(column) && StrUtil.isNotEmpty(column.getDictType())
                && StrUtil.equalsAny(column.getHtmlType(), CodegenColumnHtmlTypeEnum.SELECT.getType(),
                CodegenColumnHtmlTypeEnum.RADIO.getType(), CodegenColumnHtmlTypeEnum.CHECKBOX.getType()));
    }

    /**
     * 判断表单（新增或修改）中，是否存在必填（非空）的字段。注意：主键不计入
     *
     * 用于前端按需生成必填校验，以及对应的 import
     *
     * @param columns 字段数组
     * @return 是否存在
     */
    public boolean hasFormRequiredColumn(Collection<CodegenColumnDO> columns) {
        return anyMatch(columns, column -> isFormColumn(column) && !BooleanUtil.isTrue(column.getNullable())
                && !BooleanUtil.isTrue(column.getPrimaryKey()));
    }

    /**
     * 判断表单（新增或修改）中，是否存在日期字段（javaType 为 Date、LocalDate、LocalDateTime）
     *
     * 用于 Vben 前端的 API 按需 import Dayjs 类型
     *
     * @param columns 字段数组
     * @return 是否存在
     */
    public boolean hasFormDateColumn(Collection<CodegenColumnDO> columns) {
        return anyMatch(columns, column -> isFormColumn(column)
                && StrUtil.equalsAnyIgnoreCase(column.getJavaType(), "Date", "LocalDate", "LocalDateTime"));
    }

    /**
     * 判断多个子表的字段数组中，是否存在表单（新增或修改）中的日期字段
     *
     * @param subColumnsList 子表的字段数组的数组
     * @return 是否存在
     */
    public boolean hasSubFormDateColumn(Collection<? extends Collection<CodegenColumnDO>> subColumnsList) {
        return CollUtil.isNotEmpty(subColumnsList) && anyMatch(subColumnsList, this::hasFormDateColumn);
    }

    /**
     * 获得排除指定字段后的字段数组
     *
     * 用于主子表的子表表单：子表的 join 字段由主表关联，不会渲染为表单组件，所以需要排除后再判断
     *
     * @param columns 字段数组
     * @param exclude 排除的字段
     * @return 字段数组
     */
    public List<CodegenColumnDO> excludeColumn(Collection<CodegenColumnDO> columns, CodegenColumnDO exclude) {
        return filterList(columns, column -> column != exclude);
    }

    private static boolean isFormColumn(CodegenColumnDO column) {
        return BooleanUtil.isTrue(column.getCreateOperation()) || BooleanUtil.isTrue(column.getUpdateOperation());
    }

}
