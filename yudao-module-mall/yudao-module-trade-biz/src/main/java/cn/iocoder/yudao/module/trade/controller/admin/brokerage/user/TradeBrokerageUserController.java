package cn.iocoder.yudao.module.trade.controller.admin.brokerage.user;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.trade.controller.admin.brokerage.user.vo.*;
import cn.iocoder.yudao.module.trade.convert.brokerage.user.TradeBrokerageUserConvert;
import cn.iocoder.yudao.module.trade.dal.dataobject.brokerage.user.TradeBrokerageUserDO;
import cn.iocoder.yudao.module.trade.service.brokerage.user.TradeBrokerageUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 分销用户")
@RestController
@RequestMapping("/trade/brokerage-user")
@Validated
public class TradeBrokerageUserController {

    @Resource
    private TradeBrokerageUserService brokerageUserService;

    @PutMapping("/update-brokerage-user")
    @Operation(summary = "修改推广员")
    @PreAuthorize("@ss.hasPermission('trade:brokerage-user:update-brokerage-user')")
    public CommonResult<Boolean> updateBrokerageUser(@Valid @RequestBody TradeBrokerageUserUpdateBrokerageUserReqVO updateReqVO) {
        brokerageUserService.updateBrokerageUserId(updateReqVO.getId(), updateReqVO.getBrokerageUserId());
        return success(true);
    }

    @PutMapping("/clear-brokerage-user")
    @Operation(summary = "清除推广员")
    @PreAuthorize("@ss.hasPermission('trade:brokerage-user:clear-brokerage-user')")
    public CommonResult<Boolean> clearBrokerageUser(@Valid @RequestBody TradeBrokerageUserClearBrokerageUserReqVO updateReqVO) {
        brokerageUserService.updateBrokerageUserId(updateReqVO.getId(), null);
        return success(true);
    }

    @PutMapping("/update-brokerage-enable")
    @Operation(summary = "修改推广资格")
    @PreAuthorize("@ss.hasPermission('trade:brokerage-user:update-brokerage-enable')")
    public CommonResult<Boolean> updateBrokerageEnabled(@Valid @RequestBody TradeBrokerageUserUpdateBrokerageEnabledReqVO updateReqVO) {
        brokerageUserService.updateBrokerageEnabled(updateReqVO.getId(), updateReqVO.getBrokerageEnabled());
        return success(true);
    }

    @GetMapping("/get")
    @Operation(summary = "获得分销用户")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('trade:brokerage-user:query')")
    public CommonResult<TradeBrokerageUserRespVO> getBrokerageUser(@RequestParam("id") Long id) {
        TradeBrokerageUserDO brokerageUser = brokerageUserService.getBrokerageUser(id);
        return success(TradeBrokerageUserConvert.INSTANCE.convert(brokerageUser));
    }

    @GetMapping("/page")
    @Operation(summary = "获得分销用户分页")
    @PreAuthorize("@ss.hasPermission('trade:brokerage-user:query')")
    public CommonResult<PageResult<TradeBrokerageUserRespVO>> getBrokerageUserPage(@Valid TradeBrokerageUserPageReqVO pageVO) {
        PageResult<TradeBrokerageUserDO> pageResult = brokerageUserService.getBrokerageUserPage(pageVO);
        return success(TradeBrokerageUserConvert.INSTANCE.convertPage(pageResult));
    }

}
