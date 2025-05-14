package cn.iocoder.yudao.module.trade.controller.app.brokerage;

import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.dict.core.DictFrameworkUtils;
import cn.iocoder.yudao.module.pay.api.transfer.PayTransferApi;
import cn.iocoder.yudao.module.pay.api.transfer.dto.PayTransferRespDTO;
import cn.iocoder.yudao.module.trade.controller.admin.brokerage.vo.withdraw.BrokerageWithdrawPageReqVO;
import cn.iocoder.yudao.module.trade.controller.app.brokerage.vo.withdraw.AppBrokerageWithdrawCreateReqVO;
import cn.iocoder.yudao.module.trade.controller.app.brokerage.vo.withdraw.AppBrokerageWithdrawPageReqVO;
import cn.iocoder.yudao.module.trade.controller.app.brokerage.vo.withdraw.AppBrokerageWithdrawRespVO;
import cn.iocoder.yudao.module.trade.dal.dataobject.brokerage.BrokerageWithdrawDO;
import cn.iocoder.yudao.module.trade.enums.DictTypeConstants;
import cn.iocoder.yudao.module.trade.enums.brokerage.BrokerageWithdrawStatusEnum;
import cn.iocoder.yudao.module.trade.enums.brokerage.BrokerageWithdrawTypeEnum;
import cn.iocoder.yudao.module.trade.service.brokerage.BrokerageWithdrawService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.Objects;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;
import static cn.iocoder.yudao.framework.web.core.util.WebFrameworkUtils.getLoginUserId;

@Tag(name = "用户 APP - 分销提现")
@RestController
@RequestMapping("/trade/brokerage-withdraw")
@Validated
@Slf4j
public class AppBrokerageWithdrawController {

    @Resource
    private BrokerageWithdrawService brokerageWithdrawService;

    @Resource
    private PayTransferApi payTransferApi;

    @GetMapping("/page")
    @Operation(summary = "获得分销提现分页")
    public CommonResult<PageResult<AppBrokerageWithdrawRespVO>> getBrokerageWithdrawPage(AppBrokerageWithdrawPageReqVO pageReqVO) {
        PageResult<BrokerageWithdrawDO> pageResult = brokerageWithdrawService.getBrokerageWithdrawPage(
                BeanUtils.toBean(pageReqVO, BrokerageWithdrawPageReqVO.class).setUserId(getLoginUserId()));
        return success(BeanUtils.toBean(pageResult, AppBrokerageWithdrawRespVO.class, withdrawVO ->
                withdrawVO.setTypeName(DictFrameworkUtils.parseDictDataLabel(DictTypeConstants.BROKERAGE_WITHDRAW_TYPE, withdrawVO.getType()))
                        .setStatusName(DictFrameworkUtils.parseDictDataLabel(DictTypeConstants.BROKERAGE_WITHDRAW_STATUS, withdrawVO.getStatus()))));
    }

    @GetMapping("/get")
    @Operation(summary = "获得佣金提现")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    public CommonResult<AppBrokerageWithdrawRespVO> getBrokerageWithdraw(@RequestParam("id") Long id) {
        BrokerageWithdrawDO withdraw = brokerageWithdrawService.getBrokerageWithdraw(id);
        if (withdraw == null || ObjUtil.notEqual(withdraw.getUserId(), getLoginUserId())) {
            return success(null);
        }
        // 审核中（转账中），并且是微信转账，需要返回 mchId 用于确认收款
        AppBrokerageWithdrawRespVO withdrawVO = BeanUtils.toBean(withdraw, AppBrokerageWithdrawRespVO.class);
        if (Objects.equals(withdraw.getStatus(), BrokerageWithdrawStatusEnum.AUDIT_SUCCESS.getStatus())
                && Objects.equals(withdraw.getType(), BrokerageWithdrawTypeEnum.WECHAT_API.getType())
                && withdraw.getPayTransferId() != null) {
            PayTransferRespDTO transfer = payTransferApi.getTransfer(withdraw.getPayTransferId());
            if (transfer != null) {
                withdrawVO.setTransferChannelPackageInfo(transfer.getChannelPackageInfo())
                        .setTransferChannelMchId(transfer.getChannelMchId());
            }
        }
        return success(withdrawVO);
    }

    @PostMapping("/create")
    @Operation(summary = "创建分销提现")
    public CommonResult<Long> createBrokerageWithdraw(@RequestBody @Valid AppBrokerageWithdrawCreateReqVO createReqVO) {
        return success(brokerageWithdrawService.createBrokerageWithdraw(getLoginUserId(), createReqVO));
    }

}
