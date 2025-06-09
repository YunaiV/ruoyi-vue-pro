package cn.iocoder.yudao.module.tms.controller.admin.first.mile.request.vo;

import cn.iocoder.yudao.module.tms.controller.admin.fee.vo.TmsFeeSaveReqVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class TmsFirstMileRequestMergeReqVO {


    @Schema(description = "头程明细列表")
    private List<TmsFirstMileRequestItemMergeReqVO> items;

    //费用明细
    @Schema(description = "出运订单费用明细列表")
    private List<TmsFeeSaveReqVO> fees;


    public static class TmsFirstMileRequestItemMergeReqVO {
        @Schema(description = "头程申请项id")
        private Long id;
    }
}
