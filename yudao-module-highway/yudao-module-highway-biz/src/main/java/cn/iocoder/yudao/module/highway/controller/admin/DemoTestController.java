package cn.iocoder.yudao.module.highway.controller.admin;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 项目管理")
@RestController
@RequestMapping("/highway/demo")
@Validated
public class DemoTestController {

    @GetMapping("/")
    @Operation(summary = "获取项目信息")
    public CommonResult<String> get() {
        return success("true");
    }
}
