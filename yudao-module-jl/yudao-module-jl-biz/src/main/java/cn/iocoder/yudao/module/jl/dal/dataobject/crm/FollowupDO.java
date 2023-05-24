package cn.iocoder.yudao.module.jl.dal.dataobject.crm;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 销售线索跟进，可以是跟进客户，也可以是跟进线索 DO
 *
 * @author 惟象科技
 */
@TableName("jl_crm_followup")
@KeySequence("jl_crm_followup_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FollowupDO extends BaseDO {

    /**
     * ID
     */
    @TableId
    private Long id;
    /**
     * 内容
     */
    private String content;
    /**
     * 客户id
     */
    private Long customerId;
    /**
     * 跟进实体的 id，项目、线索、款项，客户等
     */
    private Long refId;
    /**
     * 跟进类型：日常联系、销售线索、催款等
     *
     * 枚举 {@link TODO followup_type 对应的类}
     */
    private String type;

}
