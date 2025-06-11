package cn.iocoder.yudao.module.wms.api.outbound.dto;

import cn.iocoder.yudao.framework.common.validation.InEnum;
import cn.iocoder.yudao.framework.common.validation.ValidationGroup;
import cn.iocoder.yudao.module.wms.enums.outbound.WmsOutboundStatus;
import cn.iocoder.yudao.module.wms.enums.outbound.WmsOutboundType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

import static cn.iocoder.yudao.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * @author jisencai
 * @table-fields : code,company_id,remark,audit_status,outbound_time,type,upstream_type,latest_outbound_action_id,outbound_status,upstream_id,id,upstream_code,dept_id,warehouse_id
 */
@Data
public class WmsOutboundImportReqDTO {



    /**
     * WMS出库单类型
     */
    @NotNull(message = "WMS出库单类型不能为空", groups = { ValidationGroup.create.class })
    @InEnum(WmsOutboundType.class)
    private Integer type;

    /**
     * 详情清单
     */
    @NotNull(message = "详情清单不能为空", groups = {ValidationGroup.create.class})
    private List<WmsOutboundItemSaveReqDTO> itemList;

    /**
     * WMS出库状态 ; WmsOutboundStatus : 0-未出库 , 1-部分出库 , 2-已出库
     */
    @InEnum(WmsOutboundStatus.class)
    private Integer outboundStatus;

    /**
     * 库存财务公司ID
     */
//    @NotNull(message = "库存财务公司ID不能为空", groups = {ValidationGroup.create.class})
    private Long companyId;

//    /**
//     * 库存归属部门ID
//     */
//    private Long deptId;

    /**
     * 仓库ID
     */
    @NotNull(message = "仓库ID不能为空", groups = {ValidationGroup.create.class})
    private Long warehouseId;

    /**
     * 出库时间
     */
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime outboundTime;

    /**
     * 出库动作ID，与flow关联
     */
    private Long latestOutboundActionId;

    /**
     * 备注
     */
    private String remark;


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
     * WMS来源单据类型
     */
    private Integer upstreamType;
}
