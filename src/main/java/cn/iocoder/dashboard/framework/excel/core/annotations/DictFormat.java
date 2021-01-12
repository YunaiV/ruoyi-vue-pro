package cn.iocoder.dashboard.framework.excel.core.annotations;

import cn.iocoder.dashboard.modules.system.enums.dict.DictTypeEnum;

import java.lang.annotation.*;

/**
 * 字典格式化
 *
 * 实现将字典数据的值，格式化成字典数据的标签
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface DictFormat {

    DictTypeEnum value();

}
