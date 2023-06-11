package cn.iocoder.yudao.module.promotion.controller.app.seckill;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.promotion.controller.app.seckill.vo.AppSeckillActivitiDetailRespVO;
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

    @GetMapping("/get-detail")
    @Operation(summary = "获得秒杀活动明细")
    @Parameter(name = "id", description = "活动编号", required = true, example = "1024")
    public CommonResult<AppSeckillActivitiDetailRespVO> getSeckillActivity(@RequestParam("id") Long id) {
        // TODO 芋艿：如果禁用的时候，需要抛出异常；
        AppSeckillActivitiDetailRespVO obj = new AppSeckillActivitiDetailRespVO();
        // 设置其属性的值
        obj.setId(id);
        obj.setName("晚九点限时秒杀");
        obj.setStatus(1);
        obj.setStartTime(LocalDateTime.of(2023, 6, 11, 0, 0, 0));
        obj.setEndTime(LocalDateTime.of(2023, 6, 11, 23, 59, 0));
        obj.setSpuId(633L);
        // 创建一个Product对象的列表
        List<AppSeckillActivitiDetailRespVO.Product> productList = new ArrayList<>();
        // 创建三个新的Product对象并设置其属性的值
        AppSeckillActivitiDetailRespVO.Product product1 = new AppSeckillActivitiDetailRespVO.Product();
        product1.setSkuId(1L);
        product1.setSeckillPrice(100);
        product1.setQuota(50);
        product1.setLimitCount(3);
        // 将第一个Product对象添加到列表中
        productList.add(product1);
        // 创建第二个Product对象并设置其属性的值
        AppSeckillActivitiDetailRespVO.Product product2 = new AppSeckillActivitiDetailRespVO.Product();
        product2.setSkuId(2L);
        product2.setSeckillPrice(200);
        product2.setQuota(100);
        product2.setLimitCount(4);
        // 将第二个Product对象添加到列表中
        productList.add(product2);
        // 创建第三个Product对象并设置其属性的值
        AppSeckillActivitiDetailRespVO.Product product3 = new AppSeckillActivitiDetailRespVO.Product();
        product3.setSkuId(3L);
        product3.setSeckillPrice(300);
        product3.setQuota(150);
        product3.setLimitCount(5);
        // 将第三个Product对象添加到列表中
        productList.add(product3);
        // 将Product列表设置为对象的属性值
        obj.setProducts(productList);
        return success(obj);
    }

}
