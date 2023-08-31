package cn.iocoder.yudao.module.promotion.controller.app.seckill;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.promotion.controller.app.seckill.vo.activity.AppSeckillActivityDetailRespVO;
import cn.iocoder.yudao.module.promotion.controller.app.seckill.vo.activity.AppSeckillActivityNowRespVO;
import cn.iocoder.yudao.module.promotion.controller.app.seckill.vo.activity.AppSeckillActivityPageReqVO;
import cn.iocoder.yudao.module.promotion.controller.app.seckill.vo.activity.AppSeckillActivityRespVO;
import cn.iocoder.yudao.module.promotion.controller.app.seckill.vo.config.AppSeckillConfigRespVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@Tag(name = "用户 App - 秒杀活动")
@RestController
@RequestMapping("/promotion/seckill-activity")
@Validated
public class AppSeckillActivityController {

    @GetMapping("/get-now")
    @Operation(summary = "获得当前秒杀活动") // 提供给首页使用
    // TODO 芋艿：需要增加 spring cache
    public CommonResult<AppSeckillActivityNowRespVO> getNowSeckillActivity() {
        AppSeckillActivityNowRespVO respVO = new AppSeckillActivityNowRespVO();
        respVO.setConfig(new AppSeckillConfigRespVO().setId(10L).setStartTime("01:00").setEndTime("09:59"));
        List<AppSeckillActivityRespVO> activityList = new ArrayList<>();
        AppSeckillActivityRespVO activity1 = new AppSeckillActivityRespVO();
        activity1.setId(1L);
        activity1.setName("618 大秒杀");
        activity1.setSpuId(2048L);
        activity1.setPicUrl("https://static.iocoder.cn/mall/a79f5d2ea6bf0c3c11b2127332dfe2df.jpg");
        activity1.setMarketPrice(50);
        activity1.setSeckillPrice(100);
        activityList.add(activity1);

        AppSeckillActivityRespVO activity2 = new AppSeckillActivityRespVO();
        activity2.setId(2L);
        activity2.setName("双十一大秒杀");
        activity2.setSpuId(4096L);
        activity2.setPicUrl("https://static.iocoder.cn/mall/132.jpeg");
        activity2.setMarketPrice(100);
        activity2.setSeckillPrice(200);
        activityList.add(activity2);
        respVO.setActivities(activityList);
        return success(respVO);
    }

    @GetMapping("/page")
    @Operation(summary = "获得秒杀活动分页")
    // TODO @芋艿：分页参数
    public CommonResult<PageResult<AppSeckillActivityRespVO>> getSeckillActivityPage(AppSeckillActivityPageReqVO pageReqVO) {
        List<AppSeckillActivityRespVO> activityList = new ArrayList<>();
        AppSeckillActivityRespVO activity1 = new AppSeckillActivityRespVO();
        activity1.setId(1L);
        activity1.setName("618 大秒杀");
        activity1.setSpuId(2048L);
        activity1.setPicUrl("https://static.iocoder.cn/mall/a79f5d2ea6bf0c3c11b2127332dfe2df.jpg");
        activity1.setMarketPrice(50);
        activity1.setSeckillPrice(100);
        activity1.setUnitName("个");
        activity1.setStock(1);
        activity1.setTotalStock(2);
        activityList.add(activity1);

        AppSeckillActivityRespVO activity2 = new AppSeckillActivityRespVO();
        activity2.setId(2L);
        activity2.setName("双十一大秒杀");
        activity2.setSpuId(4096L);
        activity2.setPicUrl("https://static.iocoder.cn/mall/132.jpeg");
        activity2.setMarketPrice(100);
        activity2.setSeckillPrice(200);
        activity2.setUnitName("套");
        activity2.setStock(2);
        activity2.setTotalStock(3);
        activityList.add(activity2);
        return success(new PageResult<>(activityList, 100L));
    }

    @GetMapping("/get-detail")
    @Operation(summary = "获得秒杀活动明细")
    @Parameter(name = "id", description = "活动编号", required = true, example = "1024")
    public CommonResult<AppSeckillActivityDetailRespVO> getSeckillActivity(@RequestParam("id") Long id) {
        // TODO 芋艿：如果禁用的时候，需要抛出异常；
        AppSeckillActivityDetailRespVO obj = new AppSeckillActivityDetailRespVO();
        // 设置其属性的值
        obj.setId(id);
        obj.setName("晚九点限时秒杀");
        obj.setStatus(1);
        obj.setStartTime(LocalDateTime.of(2023, 6, 16, 0, 0, 0));
        obj.setEndTime(LocalDateTime.of(2023, 6, 20, 23, 59, 0));
        obj.setSpuId(633L);
        obj.setSingleLimitCount(2);
        obj.setTotalLimitCount(3);
        obj.setStock(100);
        obj.setTotalStock(200);

        // 创建一个Product对象的列表
        List<AppSeckillActivityDetailRespVO.Product> productList = new ArrayList<>();
        // 创建三个新的Product对象并设置其属性的值
        AppSeckillActivityDetailRespVO.Product product1 = new AppSeckillActivityDetailRespVO.Product();
        product1.setSkuId(1L);
        product1.setSeckillPrice(100);
        product1.setStock(50);
        // 将第一个Product对象添加到列表中
        productList.add(product1);
        // 创建第二个Product对象并设置其属性的值
        AppSeckillActivityDetailRespVO.Product product2 = new AppSeckillActivityDetailRespVO.Product();
        product2.setSkuId(2L);
        product2.setSeckillPrice(200);
        product2.setStock(100);
        // 将第二个Product对象添加到列表中
        productList.add(product2);
        // 创建第三个Product对象并设置其属性的值
        AppSeckillActivityDetailRespVO.Product product3 = new AppSeckillActivityDetailRespVO.Product();
        product3.setSkuId(3L);
        product3.setSeckillPrice(300);
        product3.setStock(150);
        // 将第三个Product对象添加到列表中
        productList.add(product3);
        // 将Product列表设置为对象的属性值
        obj.setProducts(productList);
        return success(obj);
    }

}
