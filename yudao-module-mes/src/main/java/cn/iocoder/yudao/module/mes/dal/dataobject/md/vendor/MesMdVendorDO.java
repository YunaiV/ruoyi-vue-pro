package cn.iocoder.yudao.module.mes.dal.dataobject.md.vendor;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * MES 供应商 DO
 *
 * @author 芋道源码
 */
@TableName("mes_md_vendor")
@KeySequence("mes_md_vendor_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MesMdVendorDO extends BaseDO {

    /**
     * 供应商编号
     */
    @TableId
    private Long id;
    /**
     * 供应商编码
     */
    private String code;
    /**
     * 供应商名称
     */
    private String name;
    /**
     * 供应商简称
     */
    private String nickname;
    /**
     * 供应商英文名称
     */
    private String englishName;
    /**
     * 供应商简介
     */
    private String description;
    /**
     * 供应商LOGO地址
     */
    private String logo;
    /**
     * 供应商等级
     *
     * 字典 {@link cn.iocoder.yudao.module.mes.enums.DictTypeConstants#MES_VENDOR_LEVEL}
     */
    private String level;
    /**
     * 供应商评分
     */
    private Integer score;
    /**
     * 供应商地址
     */
    private String address;
    /**
     * 供应商官网地址
     */
    private String website;
    /**
     * 供应商邮箱地址
     */
    private String email;
    /**
     * 供应商电话
     */
    private String telephone;
    /**
     * 联系人1
     */
    private String contact1Name;
    /**
     * 联系人1-电话
     */
    private String contact1Telephone;
    /**
     * 联系人1-邮箱
     */
    private String contact1Email;
    /**
     * 联系人2
     */
    private String contact2Name;
    /**
     * 联系人2-电话
     */
    private String contact2Telephone;
    /**
     * 联系人2-邮箱
     */
    private String contact2Email;
    /**
     * 统一社会信用代码
     */
    private String creditCode;
    /**
     * 状态
     *
     * 枚举 {@link cn.iocoder.yudao.framework.common.enums.CommonStatusEnum}
     */
    private Integer status;
    /**
     * 备注
     */
    private String remark;

}
