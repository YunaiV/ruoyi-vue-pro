package cn.iocoder.yudao.module.crm.dal.dataobject.contact;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDateTime;

/**
 * crm 联系人 DO
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

    /**
     * 主键
     */
    @TableId
    private Long id;
    /**
     * 联系人名称
     */
    private String name;
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
     * 职务
     */
    private String post;
    /**
     * 客户编号
     *
     * TODO @zyna：关联的字段，也要写下
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

}
