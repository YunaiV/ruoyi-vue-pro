package cn.iocoder.yudao.module.oa.dal.dataobject.contact;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.system.enums.common.SexEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * OA 外部联系人 DO
 *
 * @author 芋道源码
 */
@TableName("oa_contact")
@KeySequence("oa_contact_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class OaContactDO extends BaseDO {

    /**
     * 联系人编号
     */
    @TableId
    private Long id;
    /**
     * 分类编号
     *
     * 关联 {@link OaContactCategoryDO#getId()}
     */
    private Long categoryId;
    /**
     * 姓名
     */
    private String name;
    /**
     * 姓名拼音
     */
    private String pinyin;
    /**
     * 性别
     *
     * 枚举 {@link SexEnum}
     */
    private Integer sex;
    /**
     * 手机号码
     */
    private String mobile;
    /**
     * 邮箱
     */
    private String email;
    /**
     * 地址
     */
    private String address;
    /**
     * 公司名称
     */
    private String companyName;
    /**
     * 公司电话
     */
    private String companyPhone;
    /**
     * 头像地址
     */
    private String avatar;
    /**
     * 备注
     */
    private String remark;

}
