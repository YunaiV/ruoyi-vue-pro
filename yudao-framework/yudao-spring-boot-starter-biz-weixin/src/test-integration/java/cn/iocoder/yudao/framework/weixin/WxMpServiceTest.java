package cn.iocoder.yudao.framework.weixin;

import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest(classes = WxMpServiceTest.Application.class)
public class WxMpServiceTest {

    @Resource
    private WxMpService wxMpService;

    @Test
    public void testGetAccessToken() throws WxErrorException {
        String accessToken = wxMpService.getAccessToken();
        System.out.println(accessToken);
    }

    @Test
    public void testGet() throws WxErrorException {
        String jsapiTicket = wxMpService.getJsapiTicket();
        System.out.println(jsapiTicket);
    }

    @SpringBootApplication
    public static class Application {

    }

}
