package cn.iocoder.yudao.module.report.controller.admin.ekycfacecheck;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.report.controller.admin.ekycfacecheck.vo.EkycChannelStatusSummaryRespVO;
import cn.iocoder.yudao.module.report.service.statisticsdata.EkycFaceCheckService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@Tag(name = "管理后台 - 通用统计数据")
@RestController
@RequestMapping("/report/ekyc-face-check")
@Validated
@Tag(name = "统计图表")
public class EkycFaceCheckController {

    @Resource
    private EkycFaceCheckService ekycFaceCheckService;

    @PreAuthorize("@ss.hasPermission('report:statistics-data:chart')")
    @GetMapping("/chart")
    @Operation(summary = "获取柱状图数据")
    public CommonResult<EkycChannelStatusSummaryRespVO> getStatusChart(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {

        // 如果参数为空，则默认设置为最近 31 天
        if (startDate == null || endDate == null) {
            endDate = LocalDate.now();
            startDate = endDate.minusDays(30); // 包含今天，共 31 天
        }

        return CommonResult.success(ekycFaceCheckService.getChannelStatusSummary(startDate, endDate));
    }

}
