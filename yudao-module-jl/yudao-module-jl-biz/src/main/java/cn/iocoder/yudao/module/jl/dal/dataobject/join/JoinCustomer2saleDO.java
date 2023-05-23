package cn.iocoder.yudao.module.jl.dal.dataobject.join;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 客户所属的销售人员 DO
 *
 * @author 惟象科技
 */
@TableName("jl_join_customer2sale")
@KeySequence("jl_join_customer2sale_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JoinCustomer2saleDO extends BaseDO {

    /**
     * 岗位ID
     */
    @TableId
    private Long id;
    /**
     * 客户id
     */
    private Long customerId;
    /**
     * 销售 id
     */
    private Long salesId;

}
