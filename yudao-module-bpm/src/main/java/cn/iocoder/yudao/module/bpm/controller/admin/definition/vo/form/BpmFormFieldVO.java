package cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.form;

import lombok.Data;

import java.util.List;

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
    /**
     * 子表单字段（处理布局组件）
     */
    private List<BpmFormFieldVO> children;

}
