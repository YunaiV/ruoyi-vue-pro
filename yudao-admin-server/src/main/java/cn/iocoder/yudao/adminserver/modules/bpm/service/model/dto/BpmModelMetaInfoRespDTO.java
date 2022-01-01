package cn.iocoder.yudao.adminserver.modules.bpm.service.model.dto;

import lombok.Data;

/**
 * BPM 流程 MetaInfo Response DTO
 * 主要用于 {@link org.activiti.engine.repository.Model#setMetaInfo(String)} 的存储
 *
 * @author 芋道源码
 */
@Data
public class BpmModelMetaInfoRespDTO {

    /**
     * 流程描述
     */
    private String description;
    /**
     * 表单编号
     */
    private Long formId;

}
