package cn.iocoder.yudao.module.mp.dal.dataobject.fansmsgres;

import lombok.*;

import java.util.*;

import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 回复粉丝消息历史表  DO
 *
 * @author 芋道源码
 */
@TableName("wx_fans_msg_res")
@KeySequence("wx_fans_msg_res_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WxFansMsgResDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Integer id;
    /**
     * 粉丝消息ID
     */
    private String fansMsgId;
    /**
     * 回复内容
     */
    private String resContent;

}
