package cn.iocoder.yudao.module.system.controller.admin.captcha;

import cn.iocoder.yudao.framework.operatelog.core.annotations.OperateLog;
import com.anji.captcha.model.common.ResponseModel;
import com.anji.captcha.model.vo.CaptchaVO;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.security.PermitAll;
import javax.servlet.http.HttpServletRequest;

/**
 * 验证码
 *
 * 问题：为什么不直接使用 anji 提供的 CaptchaController，而要另外继承？
 * 回答：管理使用 /admin-api/* 作为前缀，所以需要继承！
 *
 * @author 芋道源码
 */
@Tag(name = "管理后台 - 验证码")
@RestController("adminCaptchaController")
@RequestMapping("/system/captcha")
public class CaptchaController extends com.anji.captcha.controller.CaptchaController {

    @PostMapping({"/get"})
    @Operation(summary = "获得验证码")
    @PermitAll
    @OperateLog(enable = false) // 避免 Post 请求被记录操作日志
    @Override
    public ResponseModel get(@RequestBody CaptchaVO data, HttpServletRequest request) {
        return super.get(data, request);
    }

    @PostMapping("/check")
    @Operation(summary = "校验验证码")
    @PermitAll
    @OperateLog(enable = false) // 避免 Post 请求被记录操作日志
    @Override
    public ResponseModel check(@RequestBody CaptchaVO data, HttpServletRequest request) {
        return super.check(data, request);
    }

}
