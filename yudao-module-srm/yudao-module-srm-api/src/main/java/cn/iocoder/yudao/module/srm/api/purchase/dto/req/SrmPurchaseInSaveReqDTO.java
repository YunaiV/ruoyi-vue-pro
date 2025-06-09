package cn.iocoder.yudao.module.srm.api.purchase.dto.req;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Data
@Validated
public class SrmPurchaseInSaveReqDTO {

    /**
     * 主键
     */
    @NotNull(message = "主键ID不能为空")
    private Long id;

    /**
     * 单据号
     */
    private String code;

    /**
     * 来源单据ID
     */
    private Long upstreamId;

    /**
     * 来源单据编码
     */
    private String upstreamCode;

    /**
     * WMS来源单据类型 ; WmsBillType : 0-入库单 , 1-出库单 , 2-盘点单
     */
    private Integer upstreamType;


    @Size(min = 1, message = "明细列表至少有一个")
    private List<SrmPurchaseInSaveItemReqDTO> itemList;
}
