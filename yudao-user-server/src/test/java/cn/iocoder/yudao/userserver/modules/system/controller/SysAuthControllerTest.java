package cn.iocoder.yudao.userserver.modules.system.controller;

import cn.iocoder.yudao.coreservice.modules.system.service.social.SysSocialCoreService;
import cn.iocoder.yudao.userserver.modules.system.controller.auth.SysAuthController;
import cn.iocoder.yudao.userserver.modules.system.service.auth.SysAuthService;
import cn.iocoder.yudao.userserver.modules.system.service.sms.SysSmsCodeService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * {@link SysAuthController} 的单元测试类
 *
 * @author 宋天
 */
public class SysAuthControllerTest {

    private MockMvc mockMvc;

    @InjectMocks
    private SysAuthController sysAuthController;

    @Mock
    private SysAuthService authService;
    @Mock
    private SysSmsCodeService smsCodeService;
    @Mock
    private SysSocialCoreService socialService;

    @Before
    public void setup() {
        // 初始化
        MockitoAnnotations.openMocks(this);

        // 构建mvc环境
        mockMvc = MockMvcBuilders.standaloneSetup(sysAuthController).build();
    }

    @Test
    public void testResetPassword_success() throws Exception {
        // 模拟接口调用
        this.mockMvc.perform(post("/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"password\":\"1123\",\"code\":\"123456\"}}"))
                .andExpect(status().isOk())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void testUpdatePassword_success() throws Exception {
        // 模拟接口调用
        this.mockMvc.perform(post("/update-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"password\":\"1123\",\"code\":\"123456\",\"oldPassword\":\"1123\"}}"))
                .andExpect(status().isOk())
                .andDo(MockMvcResultHandlers.print());
    }

}
