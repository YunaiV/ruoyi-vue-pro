package cn.iocoder.yudao.module.crm.dal.dataobject.clue;

import com.sun.xml.bind.v2.TODO;
import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 线索 DO
 *
 * @author Wanwan
 */
@TableName("crm_clue")
@KeySequence("crm_clue_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrmClueDO extends BaseDO {

    /**
     * 编号，主键自增
     */
    @TableId
    private Long id;
    /**
     * 转化状态
     *
     * 枚举 {@link TODO infra_boolean_string 对应的类}
     */
    private Boolean transformStatus;
    /**
     * 跟进状态
     *
     * 枚举 {@link TODO infra_boolean_string 对应的类}
     */
    private Boolean followUpStatus;
    /**
     * 线索名称
     */
    private String name;
    /**
     * 客户id
     */
    private Long customerId;
    /**
     * 下次联系时间
     */
    private LocalDateTime contactNextTime;
    /**
     * 电话
     */
    private String telephone;
    /**
     * 手机号
     */
    private String mobile;
    /**
     * 地址
     */
    private String address;
    /**
     * 负责人的用户编号
     */
    private Long ownerUserId;
    /**
     * 最后跟进时间 TODO 添加跟进记录时更新该值
     */
    private LocalDateTime contactLastTime;
    /**
     * 备注
     */
    private String remark;

}
