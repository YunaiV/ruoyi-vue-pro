package cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.form;

import lombok.Data;

/**
 * 流程表单字段 VO
 */
@Data
public class BpmFormFieldVO {

    /**
     * 字段类型
     */
    private String type;
    /**
     * 字段标识
     */
    private String field;
    /**
     * 字段标题
     */
    private String title;

}
