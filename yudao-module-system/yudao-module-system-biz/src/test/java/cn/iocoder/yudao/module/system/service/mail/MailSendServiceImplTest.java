package cn.iocoder.yudao.module.system.service.mail;

import cn.hutool.extra.mail.MailAccount;
import cn.hutool.extra.mail.MailUtil;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MailSendServiceImplTest {

    /**
     * 用于快速测试你的邮箱账号是否正常
     */
    @Test
    @Disabled
    public void testDemo() {
        MailAccount mailAccount = new MailAccount()
                .setFrom("奥特曼 <ydym_test@163.com>")
                .setHost("smtp.163.com").setPort(465).setSslEnable(true)
                .setAuth(true).setUser("ydym_test@163.com").setPass("WBZTEINMIFVRYSOE");
        String messageId = MailUtil.send(mailAccount, "7685413@qq.com", "主题", "内容", false);
        System.out.println("发送结果：" + messageId);
    }

}
