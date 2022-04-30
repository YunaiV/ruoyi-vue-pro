package cn.iocoder.yudao.framework.mail.core.client.impl.hutool;

import cn.hutool.extra.mail.MailUtil;
import cn.iocoder.yudao.framework.mail.core.client.impl.AbstractMailClient;
import lombok.extern.slf4j.Slf4j;

import java.util.List;


/**
 * 邮件客户端实现
 *
 * @author wangjingyi
 * @date 2021/4/25 14:25
 */
@Slf4j
public class HutoolMailClient extends AbstractMailClient {

    @Override
    public String sendMail(String from, String content, String title, List<String> tos) {
        try{
            return MailUtil.send(from , title , content , false , null);
        }catch (Exception e){
            log.error(e.getMessage());
        }
        return "";
    }

}
