package cn.iocoder.yudao.module.crm.dal.dataobject.contact;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * CRM 联系人 DO
 *
 * @author 芋道源码
 */
@TableName("crm_contact")
@KeySequence("crm_contact_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContactDO extends BaseDO {

    // TODO @zyna：这个字段的顺序，是不是整理下；
    /**
     * 下次联系时间
     */
    private LocalDateTime nextTime;
    /**
     * 手机号
     */
    private String mobile;
    /**
     * 电话
     */
    private String telephone;
    /**
     * 电子邮箱
     */
    private String email;
    /**
     * 客户编号
     */
    private Long customerId;
    /**
     * 地址
     */
    private String address;
    /**
     * 备注
     */
    private String remark;
    /**
     * 最后跟进时间
     */
    private LocalDateTime lastTime;
    // TODO @zyna：这个放在最前面吧
    /**
     * 主键
     */
    @TableId
    private Long id;
    // TODO @zyna：直接上级，最好写下它关联的字段，例如说这个，应该关联 ContactDO 的 id 字段
    /**
     * 直属上级
     */
    private Long parentId;
    /**
     * 姓名
     */
    private String name;
    /**
     * 职位
     */
    private String post;
    /**
     * QQ
     */
    private Long qq;
    // TODO @zyna：wechat
    /**
     * 微信
     */
    private String webchat;
    // TODO @zyna：关联的枚举
    /**
     * 性别
     */
    private Integer sex;
    // TODO @zyna：这个字段改成 master 哈；
    /**
     * 是否关键决策人
     */
    private Boolean policyMakers;
    // TODO @zyna：应该是 Long
    /**
     * 负责人用户编号
     */
    private String ownerUserId;

}
