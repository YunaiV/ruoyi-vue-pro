package cn.iocoder.yudao.module.oa.dal.dataobject.officialdoc;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.oa.enums.DictTypeConstants;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * OA 套红模板 DO
 *
 * @author 芋道源码
 */
@TableName("oa_official_doc_template")
@KeySequence("oa_official_doc_template_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class OaOfficialDocTemplateDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 模板名称
     */
    private String name;
    /**
     * 红头名称
     *
     * 模板版式文本，不是 System 部门名称的冗余字段
     */
    private String authorityName;
    /**
     * 红头名称字号
     */
    private Integer fontSize;
    /**
     * 发文字号前缀
     *
     * 模板配置，供发文初始化文号前缀使用
     */
    private String noPrefix;
    /**
     * 印章图片地址
     *
     * 复用平台图片上传，不关联印章资产
     */
    private String sealPicUrl;
    /**
     * 分隔线类型
     *
     * 字典 {@link DictTypeConstants#OFFICIAL_DOC_SEPARATOR_TYPE}
     */
    private Integer separatorType;
    /**
     * 状态
     *
     * 枚举 {@link CommonStatusEnum}
     */
    private Integer status;
    /**
     * 显示顺序
     */
    private Integer sort;
    /**
     * 备注
     */
    private String remark;

}
