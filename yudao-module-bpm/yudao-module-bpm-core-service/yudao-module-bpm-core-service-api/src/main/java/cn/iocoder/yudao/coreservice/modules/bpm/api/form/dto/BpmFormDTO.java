package cn.iocoder.yudao.coreservice.modules.bpm.api.form.dto;

import lombok.Data;

import java.util.List;

/**
 * 工作流的表单定义 DTO
 * 用于工作流的申请表单，需要动态配置的场景
 * TODO 暂时拷贝 BpmFormDO 字段， 不知道那些字段是必须的， 后续删掉不需要的字段
 * @author jason
 */
@Data
public class BpmFormDTO {

    /**
     * 编号
     */
    private Long id;
    /**
     * 表单名
     */
    private String name;
    /**
     * 表单的配置
     */
    private String conf;
    /**
     * 表单项的数组
     *
     * 目前直接将 https://github.com/JakHuang/form-generator 生成的 JSON 串，直接保存
     * 定义：https://github.com/JakHuang/form-generator/issues/46
     */
    private List<String> fields;
    /**
     * 备注
     */
    private String remark;
}
