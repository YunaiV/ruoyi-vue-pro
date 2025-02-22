package cn.iocoder.yudao.module.iot.controller.admin.statistics;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.controller.admin.product.vo.product.IotProductRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.statistics.vo.IotStatisticsRespVO;
import cn.iocoder.yudao.module.iot.service.device.IotDeviceService;
import cn.iocoder.yudao.module.iot.service.product.IotProductCategoryService;
import cn.iocoder.yudao.module.iot.service.product.IotProductService;
import cn.iocoder.yudao.module.iot.service.thingmodel.IotThingModelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;


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


    @GetMapping("/count")
    @Operation(summary = "获取IOT首页的数据统计", description = "主要用于IOT首页的数据统计")
    public CommonResult<IotStatisticsRespVO> getSimpleProductList(){
        IotStatisticsRespVO iotStatisticsRespVO = new IotStatisticsRespVO();
        // 获取总数
        iotStatisticsRespVO.setCategoryTotal(iotProductCategoryService.getProductCategoryCount(null));
        iotStatisticsRespVO.setProductTotal(iotProductService.getProductCount(null));
        iotStatisticsRespVO.setDeviceTotal(iotDeviceService.getDeviceCount(null));

        // 获取今日新增数量
        LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        iotStatisticsRespVO.setCategoryTodayTotal(iotProductCategoryService.getProductCategoryCount(todayStart));
        iotStatisticsRespVO.setProductTodayTotal(iotProductService.getProductCount(todayStart));
        iotStatisticsRespVO.setDeviceTodayTotal(iotDeviceService.getDeviceCount(todayStart));

        return CommonResult.success(iotStatisticsRespVO);
    }
}
