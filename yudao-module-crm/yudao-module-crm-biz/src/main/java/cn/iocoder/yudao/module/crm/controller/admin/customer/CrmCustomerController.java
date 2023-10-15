package cn.iocoder.yudao.module.crm.controller.admin.customer;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 客户信息")
@RestController
@RequestMapping("/crm/customer")
@Validated
public class CrmCustomerController {

    @GetMapping("/test")
    public CommonResult<String> test() {
        return success("hello");
    }

}
