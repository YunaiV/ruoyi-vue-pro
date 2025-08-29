package cn.iocoder.yudao.module.iot.controller.admin.statistics;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.util.date.LocalDateTimeUtils;
import cn.iocoder.yudao.module.iot.controller.admin.statistics.vo.IotStatisticsDeviceMessageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.statistics.vo.IotStatisticsDeviceMessageSummaryByDateRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.statistics.vo.IotStatisticsSummaryRespVO;
import cn.iocoder.yudao.module.iot.core.enums.IotDeviceStateEnum;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceService;
import cn.iocoder.yudao.module.iot.service.device.message.IotDeviceMessageService;
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
import java.util.List;
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
    private IotDeviceMessageService deviceMessageService;

    @GetMapping("/get-summary")
    @Operation(summary = "获取全局的数据统计")
    public CommonResult<IotStatisticsSummaryRespVO> getStatisticsSummary(){
        IotStatisticsSummaryRespVO respVO = new IotStatisticsSummaryRespVO();
        // 1.1 获取总数
        respVO.setProductCategoryCount(productCategoryService.getProductCategoryCount(null));
        respVO.setProductCount(productService.getProductCount(null));
        respVO.setDeviceCount(deviceService.getDeviceCount(null));
        respVO.setDeviceMessageCount(deviceMessageService.getDeviceMessageCount(null));
        // 1.2 获取今日新增数量
        LocalDateTime todayStart = LocalDateTimeUtils.getToday();
        respVO.setProductCategoryTodayCount(productCategoryService.getProductCategoryCount(todayStart));
        respVO.setProductTodayCount(productService.getProductCount(todayStart));
        respVO.setDeviceTodayCount(deviceService.getDeviceCount(todayStart));
        respVO.setDeviceMessageTodayCount(deviceMessageService.getDeviceMessageCount(todayStart));

        // 2. 获取各个品类下设备数量统计
        respVO.setProductCategoryDeviceCounts(productCategoryService.getProductCategoryDeviceCountMap());

        // 3. 获取设备状态数量统计
        Map<Integer, Long> deviceCountMap = deviceService.getDeviceCountMapByState();
        respVO.setDeviceOnlineCount(deviceCountMap.getOrDefault(IotDeviceStateEnum.ONLINE.getState(), 0L));
        respVO.setDeviceOfflineCount(deviceCountMap.getOrDefault(IotDeviceStateEnum.OFFLINE.getState(), 0L));
        respVO.setDeviceInactiveCount(deviceCountMap.getOrDefault(IotDeviceStateEnum.INACTIVE.getState(), 0L));
        return success(respVO);
    }

    @GetMapping("/get-device-message-summary-by-date")
    @Operation(summary = "获取设备消息的数据统计")
    public CommonResult<List<IotStatisticsDeviceMessageSummaryByDateRespVO>> getDeviceMessageSummaryByDate(
            @Valid IotStatisticsDeviceMessageReqVO reqVO) {
        return success(deviceMessageService.getDeviceMessageSummaryByDate(reqVO));
    }

}
