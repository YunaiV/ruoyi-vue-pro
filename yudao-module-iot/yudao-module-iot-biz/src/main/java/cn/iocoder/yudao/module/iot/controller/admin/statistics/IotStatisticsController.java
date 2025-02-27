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
    private IotDeviceService deviceService;
    @Resource
    private IotProductCategoryService productCategoryService;
    @Resource
    private IotProductService productService;
    @Resource
    private IotDeviceLogService deviceLogService;

    // TODO @super：description 非必要，可以不写哈
    @GetMapping("/main")
    @Operation(summary = "获取首页的数据统计", description = "用于IOT首页的数据统计")
    public CommonResult<IotStatisticsRespVO> getIotMainStats(@Valid IotStatisticsReqVO reqVO){
        // TODO @super：新增 get-summary 接口，返回：总数、今日新增、数量、状态
        IotStatisticsRespVO iotStatisticsRespVO = new IotStatisticsRespVO();
        // 获取总数
        iotStatisticsRespVO.setCategoryTotal(productCategoryService.getProductCategoryCount(null));
        iotStatisticsRespVO.setProductTotal(productService.getProductCount(null));
        iotStatisticsRespVO.setDeviceTotal(deviceService.getDeviceCount(null));
        iotStatisticsRespVO.setReportTotal(deviceLogService.getDeviceLogCount(null));

        // 获取今日新增数量
        LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        iotStatisticsRespVO.setCategoryTodayTotal(productCategoryService.getProductCategoryCount(todayStart));
        iotStatisticsRespVO.setProductTodayTotal(productService.getProductCount(todayStart));
        iotStatisticsRespVO.setDeviceTodayTotal(deviceService.getDeviceCount(todayStart));
        iotStatisticsRespVO.setReportTodayTotal(deviceLogService.getDeviceLogCount(todayStart));

        // 获取各个品类下设备数量统计
        iotStatisticsRespVO.setDeviceStatsOfCategory(
            productCategoryService.getDeviceCountsOfProductCategory()
        );

        // 获取设备状态数量统计
        iotStatisticsRespVO.setOnlineTotal(deviceService.getDeviceCountByState(IotDeviceStateEnum.ONLINE.getState()));
        iotStatisticsRespVO.setOfflineTotal(deviceService.getDeviceCountByState(IotDeviceStateEnum.OFFLINE.getState()));
        iotStatisticsRespVO.setNeverOnlineTotal(deviceService.getDeviceCountByState(IotDeviceStateEnum.INACTIVE.getState()));

        // TODO @super：新增 get-log-summary 接口，返回
        // 根据传入时间范围获取设备上下行消息数量统计
        iotStatisticsRespVO.setDeviceUpMessageStats(deviceLogService.getDeviceLogUpCountByHour(null, reqVO.getStartTime(), reqVO.getEndTime()));
        iotStatisticsRespVO.setDeviceDownMessageStats(deviceLogService.getDeviceLogDownCountByHour(null, reqVO.getStartTime(), reqVO.getEndTime()));

        return CommonResult.success(iotStatisticsRespVO);
    }
}
