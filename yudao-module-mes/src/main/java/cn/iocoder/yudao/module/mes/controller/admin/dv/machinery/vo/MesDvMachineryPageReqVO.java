package cn.iocoder.yudao.module.mes.controller.admin.dv.machinery.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Set;

@Schema(description = "管理后台 - MES 设备台账分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MesDvMachineryPageReqVO extends PageParam {

    @Schema(description = "设备编码", example = "EQ-001")
    private String code;

    @Schema(description = "设备名称", example = "CNC 加工中心")
    private String name;

    @Schema(description = "品牌", example = "西门子")
    private String brand;

    @Schema(description = "设备类型编号", example = "100")
    private Long machineryTypeId;

    @Schema(description = "设备类型编号列表（含子类型，由后端自动填充）", hidden = true)
    private Set<Long> machineryTypeIds;

    @Schema(description = "所属车间编号", example = "200")
    private Long workshopId;

    @Schema(description = "设备状态", example = "1")
    private Integer status;

}
