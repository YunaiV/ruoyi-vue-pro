package cn.iocoder.yudao.module.yaya.controller.admin.health;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.tenant.core.aop.TenantIgnore;
import jakarta.annotation.security.PermitAll;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@RestController
@RequestMapping("/yaya/health")
public class YayaHealthController {

    @PermitAll
    @TenantIgnore
    @GetMapping
    public CommonResult<String> health() {
        return success("ok");
    }

}
