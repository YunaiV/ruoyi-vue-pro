package cn.iocoder.yudao.module.trade.controller.admin.brokerage.record;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.trade.controller.admin.brokerage.record.vo.TradeBrokerageRecordPageReqVO;
import cn.iocoder.yudao.module.trade.controller.admin.brokerage.record.vo.TradeBrokerageRecordRespVO;
import cn.iocoder.yudao.module.trade.convert.brokerage.record.TradeBrokerageRecordConvert;
import cn.iocoder.yudao.module.trade.dal.dataobject.brokerage.record.TradeBrokerageRecordDO;
import cn.iocoder.yudao.module.trade.service.brokerage.record.TradeBrokerageRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 佣金记录")
@RestController
@RequestMapping("/trade/brokerage-record")
@Validated
public class TradeBrokerageRecordController {

    @Resource
    private TradeBrokerageRecordService tradeBrokerageRecordService;

    @GetMapping("/get")
    @Operation(summary = "获得佣金记录")
    @Parameter(name = "id", description = "编号", required = true, example = "1024")
    @PreAuthorize("@ss.hasPermission('trade:brokerage-record:query')")
    public CommonResult<TradeBrokerageRecordRespVO> getBrokerageRecord(@RequestParam("id") Integer id) {
        TradeBrokerageRecordDO tradeBrokerageRecord = tradeBrokerageRecordService.getBrokerageRecord(id);
        return success(TradeBrokerageRecordConvert.INSTANCE.convert(tradeBrokerageRecord));
    }

    @GetMapping("/page")
    @Operation(summary = "获得佣金记录分页")
    @PreAuthorize("@ss.hasPermission('trade:brokerage-record:query')")
    public CommonResult<PageResult<TradeBrokerageRecordRespVO>> getBrokerageRecordPage(@Valid TradeBrokerageRecordPageReqVO pageVO) {
        PageResult<TradeBrokerageRecordDO> pageResult = tradeBrokerageRecordService.getBrokerageRecordPage(pageVO);
        return success(TradeBrokerageRecordConvert.INSTANCE.convertPage(pageResult));
    }

}
