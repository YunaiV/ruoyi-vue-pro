package cn.iocoder.yudao.framework.extension.core.stereotype;

import cn.iocoder.yudao.framework.extension.core.BusinessScenario;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * @description 表示带注释的类是“扩展组件”
 * @author Qingchen
 * @version 1.0.0
 * @date 2021-08-28 21:59
 * @class cn.iocoder.yudao.framework.extension.core.stereotype.Extension.java
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Component
public @interface Extension {

    /**
     * 业务 <br/>
     * 一个自负盈亏的财务主体，比如tmall、淘宝和零售通就是三个不同的业务
     * @return
     */
    String businessId() default BusinessScenario.DEFAULT_BUSINESS_ID;

    /**
     * 用例 <br/>
     * 描述了用户和系统之间的互动，每个用例提供了一个或多个场景。比如，支付订单就是一个典型的用例。
     * @return
     */
    String useCase() default BusinessScenario.DEFAULT_USECASE;

    /**
     * 场景 <br/>
     * 场景也被称为用例的实例（Instance），包括用例所有的可能情况（正常的和异常的）。比如对于"订单支付"这个用例，就有“支付宝支付”、“银行卡支付”、"微信支付"等多个场景
     * @return
     */
    String scenario() default BusinessScenario.DEFAULT_SCENARIO;
}
