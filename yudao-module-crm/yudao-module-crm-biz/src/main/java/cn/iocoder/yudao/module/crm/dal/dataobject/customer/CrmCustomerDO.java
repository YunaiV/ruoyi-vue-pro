package cn.iocoder.yudao.module.crm.dal.dataobject.customer;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sun.xml.bind.v2.TODO;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 客户 DO
 *
 * @author Wanwan
 */
@TableName("crm_customer")
@KeySequence("crm_customer_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrmCustomerDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 客户名称
     */
    private String name;
    /**
     * 跟进状态
     * <p>
     * 枚举 {@link TODO infra_boolean_string 对应的类}
     */
    private Boolean followUpStatus;
    /**
     * 锁定状态
     * <p>
     * 枚举 {@link TODO infra_boolean_string 对应的类}
     */
    private Boolean lockStatus;
    /**
     * 成交状态
     * <p>
     * 枚举 {@link TODO infra_boolean_string 对应的类}
     */
    private Boolean dealStatus;
    /**
     * 手机
     */
    private String mobile;
    /**
     * 电话
     */
    private String telephone;
    /**
     * 网址
     */
    private String website;
    /**
     * 备注
     */
    private String remark;
    /**
     * 负责人的用户编号
     */
    private Long ownerUserId;
    /**
     * 只读权限的用户编号数组
     */
    private String roUserIds;
    /**
     * 读写权限的用户编号数组
     */
    private String rwUserIds;
    /**
     * 地区编号
     */
    private Long areaId;
    /**
     * 详细地址
     */
    private String detailAddress;
    /**
     * 地理位置经度
     */
    private String longitude;
    /**
     * 地理位置维度
     */
    private String latitude;
    /**
     * 最后跟进时间
     */
    private LocalDateTime contactLastTime;
    /**
     * 下次联系时间
     */
    private LocalDateTime contactNextTime;

}
