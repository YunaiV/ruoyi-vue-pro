package cn.iocoder.yudao.module.point.dal.dataobject.signinrecord;

import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 用户签到积分 DO
 *
 * @author 芋道源码
 */
@TableName("member_sign_in_record")
@KeySequence("member_sign_in_record_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignInRecordDO extends BaseDO {

    /**
     * 签到自增id
     */
    @TableId
    private Long id;
    /**
     * 签到用户
     */
    private Integer userId;
    /**
     * 第几天签到
     */
    private Integer day;
    /**
     * 签到的分数
     */
    private Integer point;

}
