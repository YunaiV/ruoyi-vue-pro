package cn.iocoder.yudao.module.mp.controller.admin.statistics;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.mp.controller.admin.statistics.vo.*;
import cn.iocoder.yudao.module.mp.convert.statistics.MpStatisticsConvert;
import cn.iocoder.yudao.module.mp.service.statistics.MpStatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import me.chanjar.weixin.mp.bean.datacube.WxDataCubeInterfaceResult;
import me.chanjar.weixin.mp.bean.datacube.WxDataCubeMsgResult;
import me.chanjar.weixin.mp.bean.datacube.WxDataCubeUserCumulate;
import me.chanjar.weixin.mp.bean.datacube.WxDataCubeUserSummary;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "管理后台 - 公众号统计")
@RestController
@RequestMapping("/mp/statistics")
@Validated
public class MpStatisticsController {

    @Resource
    private MpStatisticsService mpStatisticsService;

    @GetMapping("/user-summary")
    @Operation(summary = "获得粉丝增减数据")
    @PreAuthorize("@ss.hasPermission('mp:statistics:query')")
    public CommonResult<List<MpStatisticsUserSummaryRespVO>> getUserSummary(MpStatisticsGetReqVO getReqVO) {
        List<WxDataCubeUserSummary> list = mpStatisticsService.getUserSummary(
                getReqVO.getAccountId(), getReqVO.getDate());
        return success(MpStatisticsConvert.INSTANCE.convertList01(list));
    }

    @GetMapping("/user-cumulate")
    @Operation(summary = "获得粉丝累计数据")
    @PreAuthorize("@ss.hasPermission('mp:statistics:query')")
    public CommonResult<List<MpStatisticsUserCumulateRespVO>> getUserCumulate(MpStatisticsGetReqVO getReqVO) {
        List<WxDataCubeUserCumulate> list = mpStatisticsService.getUserCumulate(
                getReqVO.getAccountId(), getReqVO.getDate());
        return success(MpStatisticsConvert.INSTANCE.convertList02(list));
    }

    @GetMapping("/upstream-message")
    @Operation(summary = "获取消息发送概况数据")
    @PreAuthorize("@ss.hasPermission('mp:statistics:query')")
    public CommonResult<List<MpStatisticsUpstreamMessageRespVO>> getUpstreamMessage(MpStatisticsGetReqVO getReqVO) {
        List<WxDataCubeMsgResult> list = mpStatisticsService.getUpstreamMessage(
                getReqVO.getAccountId(), getReqVO.getDate());
        return success(MpStatisticsConvert.INSTANCE.convertList03(list));
    }

    @GetMapping("/interface-summary")
    @Operation(summary = "获取消息发送概况数据")
    @PreAuthorize("@ss.hasPermission('mp:statistics:query')")
    public CommonResult<List<MpStatisticsInterfaceSummaryRespVO>> getInterfaceSummary(MpStatisticsGetReqVO getReqVO) {
        List<WxDataCubeInterfaceResult> list = mpStatisticsService.getInterfaceSummary(
                getReqVO.getAccountId(), getReqVO.getDate());
        return success(MpStatisticsConvert.INSTANCE.convertList04(list));
    }
}
