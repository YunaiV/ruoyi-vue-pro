package cn.iocoder.yudao.framework.encrypt.core.annotation;

import java.lang.annotation.*;

/**
 * 请求参数解密
 * @author Zhougang
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiDecrypt {

}
