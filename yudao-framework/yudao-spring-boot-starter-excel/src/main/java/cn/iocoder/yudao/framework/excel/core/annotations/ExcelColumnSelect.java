package cn.iocoder.yudao.framework.excel.core.annotations;

import java.lang.annotation.*;

/**
 * 给 Excel 列添加下拉选择数据
 *
 * 其中 {@link #dictType()} 和 {@link #functionName()} 二选一
 *
 * @author HUIHUI
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface ExcelColumnSelect {

    /**
     * @return 字典类型
     */
    String dictType() default "";

    /**
     * @return 获取下拉数据源的方法名称
     */
    String functionName() default "";

}
