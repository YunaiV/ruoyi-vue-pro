package cn.iocoder.yudao.module.weapp.controller.admin;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;

@Tag(name = "小程序管理")
@RestController
@RequestMapping("/weapp")
@Validated
public class WeappController {

    @GetMapping("/getWeappList")
    @Operation(summary = "获取小程序列表")
    @PreAuthorize("@ss.hasPermission('weapp:getWeappList')")
    public CommonResult<String> getWeappListByPage(){
        return CommonResult.success("true111");
    }
}
