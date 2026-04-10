package cn.iocoder.yudao.module.mes.dal.dataobject.md.client;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * MES 客户 DO
 *
 * @author 芋道源码
 */
@TableName("mes_md_client")
@KeySequence("mes_md_client_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MesMdClientDO extends BaseDO {

    /**
     * 客户编号
     */
    @TableId
    private Long id;
    /**
     * 客户编码
     */
    private String code;
    /**
     * 客户名称
     */
    private String name;
    /**
     * 客户简称
     */
    private String nickname;
    /**
     * 客户英文名称
     */
    private String englishName;
    /**
     * 客户简介
     */
    private String description;
    /**
     * 客户LOGO地址
     */
    private String logo;
    /**
     * 客户类型
     *
     * 字典 {@link cn.iocoder.yudao.module.mes.enums.DictTypeConstants#MES_CLIENT_TYPE}
     */
    private Integer type;
    /**
     * 客户地址
     */
    private String address;
    /**
     * 客户官网地址
     */
    private String website;
    /**
     * 客户邮箱地址
     */
    private String email;
    /**
     * 客户电话
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
