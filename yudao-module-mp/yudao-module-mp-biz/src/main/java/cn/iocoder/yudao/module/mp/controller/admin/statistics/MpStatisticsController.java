package cn.iocoder.yudao.module.mp.controller.admin.statistics;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.mp.convert.statistics.MpStatisticsConvert;
import cn.iocoder.yudao.module.mp.service.statistics.MpStatisticsService;
import cn.iocoder.yudao.module.mp.controller.admin.statistics.vo.MpStatisticsGetReqVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import me.chanjar.weixin.mp.bean.datacube.WxDataCubeUserSummary;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Api(tags = "管理后台 - 公众号统计")
@RestController
@RequestMapping("/mp/statistics")
@Validated
public class MpStatisticsController {

    @Resource
    private MpStatisticsService mpStatisticsService;

    @GetMapping("/user-summary")
    @ApiOperation("获得用户增减数据")
    @PreAuthorize("@ss.hasPermission('mp:statistics:query')")
    public CommonResult<List<WxDataCubeUserSummary>> getAccount(MpStatisticsGetReqVO getReqVO) {
        List<WxDataCubeUserSummary> list = mpStatisticsService.getUserSummary(getReqVO.getId(), getReqVO.getDate());
        return success(MpStatisticsConvert.INSTANCE.convertList01(list));
    }

}
