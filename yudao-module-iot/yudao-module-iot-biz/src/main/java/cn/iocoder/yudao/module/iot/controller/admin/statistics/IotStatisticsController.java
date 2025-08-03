package cn.iocoder.yudao.module.iot.controller.admin.statistics;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.controller.admin.statistics.vo.IotStatisticsDeviceMessageSummaryRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.statistics.vo.IotStatisticsReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.statistics.vo.IotStatisticsSummaryRespVO;
import cn.iocoder.yudao.module.iot.enums.device.IotDeviceStateEnum;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceService;
import cn.iocoder.yudao.module.iot.service.device.data.IotDeviceLogService;
import cn.iocoder.yudao.module.iot.service.product.IotProductCategoryService;
import cn.iocoder.yudao.module.iot.service.product.IotProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

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

    @GetMapping("/get-summary")
    @Operation(summary = "获取 IoT 数据统计")
    public CommonResult<IotStatisticsSummaryRespVO> getIotStatisticsSummary(){
        IotStatisticsSummaryRespVO respVO = new IotStatisticsSummaryRespVO();
        // 1.1 获取总数
        respVO.setProductCategoryCount(productCategoryService.getProductCategoryCount(null));
        respVO.setProductCount(productService.getProductCount(null));
        respVO.setDeviceCount(deviceService.getDeviceCount(null));
        respVO.setDeviceMessageCount(deviceLogService.getDeviceLogCount(null));
        // 1.2 获取今日新增数量
        // TODO @super：使用 LocalDateTimeUtils.getToday()
        LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        respVO.setProductCategoryTodayCount(productCategoryService.getProductCategoryCount(todayStart));
        respVO.setProductTodayCount(productService.getProductCount(todayStart));
        respVO.setDeviceTodayCount(deviceService.getDeviceCount(todayStart));
        respVO.setDeviceMessageTodayCount(deviceLogService.getDeviceLogCount(todayStart));

        // 2. 获取各个品类下设备数量统计
        respVO.setProductCategoryDeviceCounts(productCategoryService.getProductCategoryDeviceCountMap());

        // 3. 获取设备状态数量统计
        Map<Integer, Long> deviceCountMap = deviceService.getDeviceCountMapByState();
        respVO.setDeviceOnlineCount(deviceCountMap.getOrDefault(IotDeviceStateEnum.ONLINE.getState(), 0L));
        respVO.setDeviceOfflineCount(deviceCountMap.getOrDefault(IotDeviceStateEnum.OFFLINE.getState(), 0L));
        respVO.setDeviceInactiveCount(deviceCountMap.getOrDefault(IotDeviceStateEnum.INACTIVE.getState(), 0L));
        return success(respVO);
    }

    // TODO @super：要不干掉 IotStatisticsReqVO 参数，直接使用 @RequestParam 接收，简单一些。
    @GetMapping("/get-log-summary")
    @Operation(summary = "获取 IoT 设备上下行消息数据统计")
    public CommonResult<IotStatisticsDeviceMessageSummaryRespVO> getIotStatisticsDeviceMessageSummary(
            @Valid IotStatisticsReqVO reqVO) {
        return success(new IotStatisticsDeviceMessageSummaryRespVO()
                .setDownstreamCounts(deviceLogService.getDeviceLogUpCountByHour(null, reqVO.getStartTime(), reqVO.getEndTime()))
                .setDownstreamCounts((deviceLogService.getDeviceLogDownCountByHour(null, reqVO.getStartTime(), reqVO.getEndTime()))));
    }

}
