package cn.iocoder.yudao.module.pay.controller.admin.demo;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.pay.controller.admin.demo.vo.PayDemoOrderCreateReqVO;
import cn.iocoder.yudao.module.pay.controller.admin.demo.vo.PayDemoOrderRespVO;
import cn.iocoder.yudao.module.pay.convert.demo.PayDemoOrderConvert;
import cn.iocoder.yudao.module.pay.dal.dataobject.demo.PayDemoOrderDO;
import cn.iocoder.yudao.module.pay.service.demo.PayDemoOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;

@Tag(name = "管理后台 - 示例订单")
@RestController
@RequestMapping("/pay/demo-order")
@Validated
public class PayDemoOrderController {

    @Resource
    private PayDemoOrderService payDemoOrderService;

    @PostMapping("/create")
    @Operation(summary = "创建示例订单")
    public CommonResult<Long> createDemoOrder(@Valid @RequestBody PayDemoOrderCreateReqVO createReqVO) {
        return success(payDemoOrderService.createDemoOrder(getLoginUserId(), createReqVO));
    }

    @GetMapping("/page")
    @Operation(summary = "获得示例订单分页")
    public CommonResult<PageResult<PayDemoOrderRespVO>> getDemoOrderPage(@Valid PageParam pageVO) {
        PageResult<PayDemoOrderDO> pageResult = payDemoOrderService.getDemoOrderPage(pageVO);
        return success(PayDemoOrderConvert.INSTANCE.convertPage(pageResult));
    }

}
