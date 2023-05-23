package cn.iocoder.yudao.module.jl.dal.dataobject.join;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 销售线索中竞争对手的报价 DO
 *
 * @author 惟象科技
 */
@TableName("jl_join_saleslead2competitor")
@KeySequence("jl_join_saleslead2competitor_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JoinSaleslead2competitorDO extends BaseDO {

    /**
     * 岗位ID
     */
    @TableId
    private Long id;
    /**
     * 销售线索 id
     */
    private Long salesleadId;
    /**
     * 竞争对手
     */
    private Long competitorId;
    /**
     * 竞争对手的报价
     */
    private Long competitorQuotation;

}
