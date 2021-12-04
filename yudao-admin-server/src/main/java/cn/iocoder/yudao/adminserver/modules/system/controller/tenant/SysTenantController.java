package cn.iocoder.yudao.adminserver.modules.system.controller.tenant;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@Api(tags = "租户")
@RestController
@RequestMapping("/system/tenant")
public class SysTenantController {

    @GetMapping("/get-id-by-name")
    @ApiOperation(value = "使用租户名，获得租户编号", notes = "登录界面，根据用户的租户名，获得租户编号")
    @ApiImplicitParam(name = "name", value = "租户名", required = true, example = "芋道源码", dataTypeClass = Long.class)
    public CommonResult<Long> getTenantIdByName(@RequestParam("name") String name) {
        if (Objects.equals("芋道源码", name)) {
            return CommonResult.success(0L);
        }
        return CommonResult.success(null);
    }

}
