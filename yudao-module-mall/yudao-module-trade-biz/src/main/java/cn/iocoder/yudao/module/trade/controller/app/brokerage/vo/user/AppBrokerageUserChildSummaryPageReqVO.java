package cn.iocoder.yudao.module.trade.controller.app.brokerage.vo.user;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.SortingField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "用户 App - 下级分销统计分页 Request VO")
@Data
public class AppBrokerageUserChildSummaryPageReqVO extends PageParam {

    public static final String SORT_FIELD_USER_COUNT = "userCount";
    public static final String SORT_FIELD_ORDER_COUNT = "orderCount";
    public static final String SORT_FIELD_PRICE = "price";

    @Schema(description = "用户昵称", example = "李") // 模糊匹配
    private String nickname;

    @Schema(description = "排序字段", example = "userCount")
    private SortingField sortingField;

    @Schema(description = "下级的级别", example = "1") // 1 - 直接下级；2 - 间接下级
    private Integer level;

}
