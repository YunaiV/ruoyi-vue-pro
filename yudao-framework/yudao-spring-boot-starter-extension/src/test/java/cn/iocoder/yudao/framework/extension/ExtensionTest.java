package cn.iocoder.yudao.framework.extension;

import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.framework.extension.core.BusinessScenario;
import cn.iocoder.yudao.framework.extension.core.context.ExtensionExecutor;
import cn.iocoder.yudao.framework.extension.pay.PayExtensionPoint;
import cn.iocoder.yudao.framework.extension.pay.command.TransactionsCommand;
import cn.iocoder.yudao.framework.extension.pay.domain.TransactionsResult;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.math.BigDecimal;

/**
 * @description 
 * @author Qingchen
 * @version 1.0.0
 * @date 2021-08-30 10:30
 * @class cn.iocoder.yudao.framework.extension.ExtensionTest.java
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Application.class)
@Slf4j
public class ExtensionTest {

    @Autowired
    private ExtensionExecutor extensionExecutor;

    @Test
    public void unifiedOrder() {
        final BusinessScenario scenario = BusinessScenario.valueOf("pay", "jsapi", "wechat");
        final TransactionsCommand command = new TransactionsCommand(IdUtil.objectId(), new BigDecimal(105), "Image形象店-深圳腾大-QQ公仔", "https://www.weixin.qq.com/wxpay/pay.php");
        final TransactionsResult result = extensionExecutor.execute(PayExtensionPoint.class, scenario, extension -> extension.unifiedOrder(command));
        log.info("result is: {}", JSONUtil.toJsonStr(result));
        Assert.assertSame("wechat", result.getChannel());
    }
}
