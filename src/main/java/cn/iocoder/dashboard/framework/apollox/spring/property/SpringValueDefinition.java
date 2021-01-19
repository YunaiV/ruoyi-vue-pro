package cn.iocoder.dashboard.framework.apollox.spring.property;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Spring Value 定义
 */
@Getter
@AllArgsConstructor
public class SpringValueDefinition {

    /**
     * KEY
     *
     * 即在 Config 中的属性 KEY 。
     */
    private final String key;
    /**
     * 占位符
     */
    private final String placeholder;
    /**
     * 属性名
     */
    private final String propertyName;

}
