package cn.iocoder.yudao.module.tms.controller.admin.transfer.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public class TmsTransferOffStatusReqVO {


    //开启、关闭
    @Schema(description = "调拨单项,true/开启，false/关闭", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "调拨单开关状态不能为空")
    private Boolean enable;

    @Schema(description = "调拨单项")
    @Size(min = 1, message = "调拨单项至少一个")
    @NotNull(message = "调拨单项ID集合不能为空")
    private List<Long> itemIds;
}
