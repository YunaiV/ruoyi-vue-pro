package cn.iocoder.yudao.module.crm.controller.admin.statistics.vo.customer;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;


@Schema(description = "管理后台 - CRM 数据统计 员工客户分析 VO")
@Data
public class CrmStatisticsCustomerCountVO {

    /**
     * 时间轴
     * <p>
     * group by DATE_FORMAT(create_date, '%Y%m')
     */
    @Schema(description = "时间轴", requiredMode = Schema.RequiredMode.REQUIRED, example = "202401")
    private String category;

    /**
     * 数量是个特别“抽象”的概念，在不同排行下，代表不同含义
     * <p>
     * 1. 金额：合同金额排行、回款金额排行
     * 2. 个数：签约合同排行、产品销量排行、产品销量排行、新增客户数排行、新增联系人排行、跟进次数排行、跟进客户数排行
     */
    @Schema(description = "数量", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer count;

}
