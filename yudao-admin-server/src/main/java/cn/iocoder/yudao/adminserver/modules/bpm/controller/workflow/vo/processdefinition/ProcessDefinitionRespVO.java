package cn.iocoder.yudao.adminserver.modules.bpm.controller.workflow.vo.processdefinition;

import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * @author yunlong.li
 */
@Data
@ApiModel("流程定义数据返回 Request VO")
public class ProcessDefinitionRespVO {

    private String formKey;
    private String id;
    private String key;
    private String name;
    private Integer version;
}
