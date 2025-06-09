package cn.iocoder.yudao.module.srm.api.purchase.dto.req;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Data
@Validated
public class SrmReturnSaveReqDTO {

    @NotNull(message = "ID不能为null")
    private Long id;

    /**
     * 来源单据ID
     */
    @NotNull(message = "来源单据ID不能为空")
    private Long upstreamId;

    /**
     * 来源单据编码
     */
    private String upstreamCode;

    /**
     * 上游单据类型 ;
     */
    @NotNull(message = "来源单据类型不能为空")
    private Integer upstreamType;

    /**
     * 出库单明细
     */
    @Size(min = 1, message = "明细不能为空")
    private List<@Valid SrmReturnSaveItemReqDTO> items;
}
