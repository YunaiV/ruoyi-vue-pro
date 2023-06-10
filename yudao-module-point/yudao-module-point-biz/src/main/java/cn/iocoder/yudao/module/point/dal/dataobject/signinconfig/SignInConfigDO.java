package cn.iocoder.yudao.module.point.dal.dataobject.signinconfig;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 积分签到规则 DO
 *
 * @author QingX
 */
@TableName("member_sign_in_config")
@KeySequence("member_sign_in_config_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignInConfigDO extends BaseDO {

    /**
     * 规则自增主键
     */
    @TableId
    private Integer id;
    /**
     * 签到第x天
     */
    private Integer day;
    /**
     * 签到天数对应分数
     */
    private Integer point;

}
