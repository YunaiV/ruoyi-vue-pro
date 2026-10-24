package cn.iocoder.yudao.module.trade.controller.app.aftersale;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.trade.controller.app.aftersale.vo.log.AppAfterSaleLogRespVO;
import cn.iocoder.yudao.module.trade.dal.dataobject.aftersale.AfterSaleDO;
import cn.iocoder.yudao.module.trade.dal.dataobject.aftersale.AfterSaleLogDO;
import cn.iocoder.yudao.module.trade.service.aftersale.AfterSaleLogService;
import cn.iocoder.yudao.module.trade.service.aftersale.AfterSaleService;
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

import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils.getLoginUserId;
import static cn.iocoder.yudao.module.trade.enums.ErrorCodeConstants.AFTER_SALE_NOT_FOUND;

@Tag(name = "用户 App - 售后日志")
@RestController
@RequestMapping("/trade/after-sale-log")
@Validated
@Slf4j
public class AppAfterSaleLogController {

    @Resource
    private AfterSaleService afterSaleService;
    @Resource
    private AfterSaleLogService afterSaleLogService;

    @GetMapping("/list")
    @Operation(summary = "获得售后日志列表")
    @Parameter(name = "afterSaleId", description = "售后编号", required = true, example = "1")
    public CommonResult<List<AppAfterSaleLogRespVO>> getAfterSaleLogList(
            @RequestParam("afterSaleId") Long afterSaleId) {
        // 校验售后单属于当前用户。注意：日志的 userId 是操作人（可能是管理员、系统），不能用于归属过滤
        AfterSaleDO afterSale = afterSaleService.getAfterSale(getLoginUserId(), afterSaleId);
        if (afterSale == null) {
            throw exception(AFTER_SALE_NOT_FOUND);
        }
        List<AfterSaleLogDO> logs = afterSaleLogService.getAfterSaleLogList(afterSaleId);
        return success(BeanUtils.toBean(logs, AppAfterSaleLogRespVO.class));
    }

}
