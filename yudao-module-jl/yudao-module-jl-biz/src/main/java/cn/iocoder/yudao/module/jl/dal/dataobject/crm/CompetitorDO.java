package cn.iocoder.yudao.module.jl.dal.dataobject.crm;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 友商 DO
 *
 * @author 惟象科技
 */
@TableName("jl_crm_competitor")
@KeySequence("jl_crm_competitor_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompetitorDO extends BaseDO {

    /**
     * 岗位ID
     */
    @TableId
    private Long id;
    /**
     * 公司名
     */
    private String name;
    /**
     * 联系人
     */
    private String contactName;
    /**
     * 手机号
     */
    private String phone;
    /**
     * 友商类型
     */
    private String type;
    /**
     * 优势
     */
    private String advantage;
    /**
     * 劣势
     */
    private String disadvantage;
    /**
     * 备注
     */
    private String mark;

}
