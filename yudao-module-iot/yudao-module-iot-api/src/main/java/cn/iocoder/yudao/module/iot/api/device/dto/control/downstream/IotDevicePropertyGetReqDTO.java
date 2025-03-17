package cn.iocoder.yudao.module.iot.api.device.dto.control.downstream;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.List;

// TODO @芋艿：从 server => plugin => device 是否有必要？从阿里云 iot 来看，没有这个功能？！
// TODO @芋艿：是不是改成 read 更好？在看看阿里云的 topic 设计
/**
 * IoT 设备【属性】获取 Request DTO
 *
 * @author 芋道源码
 */
@Data
public class IotDevicePropertyGetReqDTO extends IotDeviceDownstreamAbstractReqDTO {

    /**
     * 属性标识数组
     */
    @NotEmpty(message = "属性标识数组不能为空")
    private List<String> identifiers;

}
