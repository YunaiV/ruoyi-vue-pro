package cn.iocoder.yudao.framework.common.validation;

import cn.iocoder.yudao.framework.common.core.ArrayValuable;
import cn.iocoder.yudao.framework.common.core.EmptyArrayValuable;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * 校验字段的值必须属于制定枚举或者数据字典类型。
 * </p>
 * 用法：
 * <ul>
 *     <li>枚举校验：@InEnum(value = CommonStatusEnum.class)</li>
 *     <li>字典校验：@InEnum(dictType = "common_status")</li>
 * </ul>
 * value 和 dictType 互斥，不能同时设置
 * </p>
 *
 * value默认值为EmptyArrayValuable.class，如果使用枚举校验还是枚举类去实现ArrayValuable
 */
@Target({
        ElementType.METHOD,
        ElementType.FIELD,
        ElementType.ANNOTATION_TYPE,
        ElementType.CONSTRUCTOR,
        ElementType.PARAMETER,
        ElementType.TYPE_USE
})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(
        validatedBy = {InEnumValidator.class, InEnumCollectionValidator.class}
)
public @interface InEnum {

    /**
     * @return 实现 ArrayValuable 接口的类
     */
    Class<? extends ArrayValuable<?>> value() default EmptyArrayValuable.class;

    String message() default "必须在指定范围 {value}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /**
     * 数据字典类型
     * @return 数据字典类型，与value互斥
     */
    String dictType() default "";

    String dictSql() default "select value from system_dict_data where dict_type = ? and status = 0";


}
