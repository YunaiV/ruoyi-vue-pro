package cn.iocoder.yudao.module.pay.controller.app.transfer;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.pay.service.transfer.PayTransferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "用户 APP - 转账单")
@RestController
@RequestMapping("/pay/transfer")
@Validated
@Slf4j
public class AppPayTransferController {

    @Resource
    private PayTransferService transferService;

    @GetMapping("/sync")
    @Operation(summary = "同步转账单") // 目的：解决微信转账的异步回调可能有延迟的问题
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    public CommonResult<Boolean> syncTransfer(@RequestParam("id") Long id) {
        transferService.syncTransfer(id);
        return success(true);
    }

}
