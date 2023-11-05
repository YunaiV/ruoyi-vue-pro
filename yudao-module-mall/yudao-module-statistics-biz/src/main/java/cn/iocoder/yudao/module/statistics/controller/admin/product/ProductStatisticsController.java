package cn.iocoder.yudao.module.statistics.controller.admin.product;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.statistics.dal.mysql.product.ProductSpuStatisticsDO;
import cn.iocoder.yudao.module.statistics.dal.mysql.product.ProductStatisticsDO;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "管理后台 - 商品统计")
@RestController
@RequestMapping("/statistics/product")
@Validated
@Slf4j
public class ProductStatisticsController {

    // TODO @麦子：返回 ProductStatisticsComparisonResp， 里面有两个字段，一个是选择的时间范围的合计结果，一个是对比的时间范围的合计结果；
    // 例如说，选择时间范围是 2023-10-01 ~ 2023-10-02，那么对比就是 2023-09-30，再倒推 2 天；
    public CommonResult<Object> getProductStatisticsComparison() {
        return null;
    }

    // TODO @麦子：查询指定时间范围内的商品统计数据；DO 到时需要改成 VO 哈
    public CommonResult<List<ProductStatisticsDO>> getProductStatisticsList(
            LocalDateTime[] times) {
        return null;
    }

    // TODO @麦子：查询指定时间范围内的商品 SPU 统计数据；DO 到时需要改成 VO 哈
    // 入参是分页参数 + 时间范围 + 排序字段
    public CommonResult<PageResult<ProductSpuStatisticsDO>> getProductSpuStatisticsPage() {
        return null;
    }

}
