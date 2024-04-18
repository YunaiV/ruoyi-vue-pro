package cn.iocoder.yudao.module.bpm.framework.flowable.core.enums;

// TODO @jason：这个类，挪到 BpmnModelConstants 里，会不会好点，因为后续 BPMN 标准也需要使用这些字段哈；
/**
 * 仿钉钉快搭 JSON 常量信息
 *
 * @author jason
 */
public interface SimpleModelConstants {

    // TODO @jason：改成 FORM_FIELD_PERMISSION_ELEMENT 会不会更精准哈；
    /**
     * 流程表单字段权限, 用于标记字段权限
     */
    String FIELDS_PERMISSION = "fieldsPermission";
    // TODO @jason：改成 FORM_FIELD_PERMISSION_ELEMENT_FIELD_ATTRIBUTE 会不会更精准哈；
    /**
     * 字段属性
     */
    String FIELD_ATTRIBUTE = "field";
    // TODO @jason：改成 FORM_FIELD_PERMISSION_ELEMENT_PERMISSION_ATTRIBUTE 会不会更精准哈；
    /**
     * 权限属性
     */
    String PERMISSION_ATTRIBUTE = "permission";

}
