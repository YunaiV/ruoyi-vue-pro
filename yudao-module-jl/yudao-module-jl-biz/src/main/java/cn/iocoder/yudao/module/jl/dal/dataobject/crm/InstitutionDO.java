package cn.iocoder.yudao.module.jl.dal.dataobject.crm;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * CRM 模块的机构/公司 DO
 *
 * @author 芋道源码
 */
@TableName("jl_crm_institution")
@KeySequence("jl_crm_institution_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InstitutionDO extends BaseDO {

    /**
     * 岗位ID
     */
    @TableId
    private Long id;
    /**
     * 省份
     */
    private String province;
    /**
     * 城市
     */
    private String city;
    /**
     * 名字
     */
    private String name;
    /**
     * 详细地址
     */
    private String address;
    /**
     * 备注信息
     */
    private String mark;
    /**
     * 机构类型
     *
     * 枚举 {@link TODO institution_type 对应的类}
     */
    private String type;

}
