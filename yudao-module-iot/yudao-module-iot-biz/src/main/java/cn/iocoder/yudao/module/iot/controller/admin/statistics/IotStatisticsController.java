package cn.iocoder.yudao.module.iot.controller.admin.statistics;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.controller.admin.statistics.vo.IotStatisticsReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.statistics.vo.IotStatisticsRespVO;
import cn.iocoder.yudao.module.iot.enums.device.IotDeviceStateEnum;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceService;
import cn.iocoder.yudao.module.iot.service.device.data.IotDeviceLogService;
import cn.iocoder.yudao.module.iot.service.product.IotProductCategoryService;
import cn.iocoder.yudao.module.iot.service.product.IotProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;


@Tag(name = "管理后台 - IoT 数据统计")
@RestController
@RequestMapping("/iot/statistics")
@Validated
public class IotStatisticsController {

    @Resource
    private IotDeviceService iotDeviceService;

    @Resource
    private IotProductCategoryService iotProductCategoryService;

    @Resource
    private IotProductService iotProductService;

    @Resource
    private IotDeviceLogService iotDeviceLogService;


    @GetMapping("/main")
    @Operation(summary = "获取IOT首页的数据统计", description = "用于IOT首页的数据统计")
    public CommonResult<IotStatisticsRespVO> getIotMainStats(@Valid IotStatisticsReqVO reqVO){
        IotStatisticsRespVO iotStatisticsRespVO = new IotStatisticsRespVO();
        // 获取总数
        iotStatisticsRespVO.setCategoryTotal(iotProductCategoryService.getProductCategoryCount(null));
        iotStatisticsRespVO.setProductTotal(iotProductService.getProductCount(null));
        iotStatisticsRespVO.setDeviceTotal(iotDeviceService.getDeviceCount(null));
        iotStatisticsRespVO.setReportTotal(iotDeviceLogService.getDeviceLogCount(null));

        // 获取今日新增数量
        LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        iotStatisticsRespVO.setCategoryTodayTotal(iotProductCategoryService.getProductCategoryCount(todayStart));
        iotStatisticsRespVO.setProductTodayTotal(iotProductService.getProductCount(todayStart));
        iotStatisticsRespVO.setDeviceTodayTotal(iotDeviceService.getDeviceCount(todayStart));
        iotStatisticsRespVO.setReportTodayTotal(iotDeviceLogService.getDeviceLogCount(todayStart));

        // 获取各个品类下设备数量统计
        iotStatisticsRespVO.setDeviceStatsOfCategory(
            iotProductCategoryService.getDeviceCountsOfProductCategory()
        );

        // 获取设备状态数量统计
        iotStatisticsRespVO.setOnlineTotal(iotDeviceService.getDeviceCountByState(IotDeviceStateEnum.ONLINE.getState()));
        iotStatisticsRespVO.setOfflineTotal(iotDeviceService.getDeviceCountByState(IotDeviceStateEnum.OFFLINE.getState()));
        iotStatisticsRespVO.setNeverOnlineTotal(iotDeviceService.getDeviceCountByState(IotDeviceStateEnum.INACTIVE.getState()));

        // 根据传入时间范围获取设备上下行消息数量统计
        iotStatisticsRespVO.setDeviceUpMessageStats(iotDeviceLogService.getDeviceLogUpCountByHour(null, reqVO.getStartTime(), reqVO.getEndTime()));
        iotStatisticsRespVO.setDeviceDownMessageStats(iotDeviceLogService.getDeviceLogDownCountByHour(null, reqVO.getStartTime(), reqVO.getEndTime()));

        return CommonResult.success(iotStatisticsRespVO);
    }
}
