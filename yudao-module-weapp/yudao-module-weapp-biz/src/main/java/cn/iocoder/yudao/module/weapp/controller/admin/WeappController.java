package cn.iocoder.yudao.module.weapp.controller.admin;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
    public CommonResult<String> getWeappListByPage(){
        return CommonResult.success("true");
    }
}
