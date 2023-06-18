package cn.iocoder.yudao.module.promotion.controller.app.combination;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.promotion.controller.app.combination.vo.activity.AppCombinationActivityDetailRespVO;
import cn.iocoder.yudao.module.promotion.controller.app.combination.vo.activity.AppCombinationActivityRespVO;
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

@Tag(name = "用户 APP - 拼团活动")
@RestController
@RequestMapping("/promotion/combination-activity")
@Validated
public class AppCombinationActivityController {

    @GetMapping("/list")
    @Operation(summary = "获得拼团活动列表", description = "用于小程序首页")
    // TODO 芋艿：增加 Spring Cache
    // TODO 芋艿：缺少 swagger 注解
    public CommonResult<List<AppCombinationActivityRespVO>> getCombinationActivityList(
            @RequestParam(name = "count", defaultValue = "6") Integer count) {
        List<AppCombinationActivityRespVO> activityList = new ArrayList<>();
        AppCombinationActivityRespVO activity1 = new AppCombinationActivityRespVO();
        activity1.setId(1L);
        activity1.setName("618 大拼团");
        activity1.setUserSize(3);
        activity1.setSpuId(2048L);
        activity1.setPicUrl("https://demo26.crmeb.net/uploads/attach/2021/11/15/a79f5d2ea6bf0c3c11b2127332dfe2df.jpg");
        activity1.setMarketPrice(50);
        activity1.setCombinationPrice(100);
        activityList.add(activity1);

        AppCombinationActivityRespVO activity2 = new AppCombinationActivityRespVO();
        activity2.setId(2L);
        activity2.setName("双十一拼团");
        activity2.setUserSize(5);
        activity2.setSpuId(4096L);
        activity2.setPicUrl("https://thirdwx.qlogo.cn/mmopen/vi_32/Q0j4TwGTfTKXMYJOomfp7cebz3cIeb8sHk3GGSIJtWEgREe3j7J1WoAbTvIOicpcNdFkWAziatBSMod8b5RyS4CQ/132");
        activity2.setMarketPrice(100);
        activity2.setCombinationPrice(200);
        activityList.add(activity2);

        return success(activityList);
    }

    @GetMapping("/page")
    @Operation(summary = "获得拼团活动分页")
    public CommonResult<PageResult<AppCombinationActivityRespVO>> getCombinationActivityPage(PageParam pageParam) {
        List<AppCombinationActivityRespVO> activityList = new ArrayList<>();
        AppCombinationActivityRespVO activity1 = new AppCombinationActivityRespVO();
        activity1.setId(1L);
        activity1.setName("618 大拼团");
        activity1.setUserSize(3);
        activity1.setSpuId(2048L);
        activity1.setPicUrl("商品图片地址");
        activity1.setMarketPrice(50);
        activity1.setCombinationPrice(100);
        activityList.add(activity1);

        AppCombinationActivityRespVO activity2 = new AppCombinationActivityRespVO();
        activity2.setId(2L);
        activity2.setName("双十一拼团");
        activity2.setUserSize(5);
        activity2.setSpuId(4096L);
        activity2.setPicUrl("商品图片地址");
        activity2.setMarketPrice(100);
        activity2.setCombinationPrice(200);
        activityList.add(activity2);

        return success(new PageResult<>(activityList, 2L));
    }

    @GetMapping("/get-detail")
    @Operation(summary = "获得拼团活动明细")
    @Parameter(name = "id", description = "活动编号", required = true, example = "1024")
    public CommonResult<AppCombinationActivityDetailRespVO> getCombinationActivityDetail(@RequestParam("id") Long id) {
        // TODO 芋艿：如果禁用的时候，需要抛出异常；
        AppCombinationActivityDetailRespVO obj = new AppCombinationActivityDetailRespVO();
        // 设置其属性的值
        obj.setId(id);
        obj.setName("晚九点限时秒杀");
        obj.setStatus(1);
        obj.setStartTime(LocalDateTime.of(2023, 6, 15, 0, 0, 0));
        obj.setEndTime(LocalDateTime.of(2023, 6, 15, 23, 59, 0));
        obj.setUserSize(2);
        obj.setSuccessCount(100);
        obj.setSpuId(633L);
        // 创建一个Product对象的列表
        List<AppCombinationActivityDetailRespVO.Product> productList = new ArrayList<>();
        // 创建三个新的Product对象并设置其属性的值
        AppCombinationActivityDetailRespVO.Product product1 = new AppCombinationActivityDetailRespVO.Product();
        product1.setSkuId(1L);
        product1.setCombinationPrice(100);
        product1.setQuota(50);
        product1.setLimitCount(3);
        // 将第一个Product对象添加到列表中
        productList.add(product1);
        // 创建第二个Product对象并设置其属性的值
        AppCombinationActivityDetailRespVO.Product product2 = new AppCombinationActivityDetailRespVO.Product();
        product2.setSkuId(2L);
        product2.setCombinationPrice(200);
        product2.setQuota(100);
        product2.setLimitCount(4);
        // 将第二个Product对象添加到列表中
        productList.add(product2);
        // 创建第三个Product对象并设置其属性的值
        AppCombinationActivityDetailRespVO.Product product3 = new AppCombinationActivityDetailRespVO.Product();
        product3.setSkuId(3L);
        product3.setCombinationPrice(300);
        product3.setQuota(150);
        product3.setLimitCount(5);
        // 将第三个Product对象添加到列表中
        productList.add(product3);
        // 将Product列表设置为对象的属性值
        obj.setProducts(productList);
        return success(obj);
    }

}
