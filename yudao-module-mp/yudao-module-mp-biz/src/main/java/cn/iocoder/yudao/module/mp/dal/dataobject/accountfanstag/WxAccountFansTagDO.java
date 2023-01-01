package cn.iocoder.yudao.module.mp.dal.dataobject.accountfanstag;

import lombok.*;

import java.util.*;

import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 粉丝标签关联 DO
 *
 * @author 芋道源码
 */
@TableName("wx_account_fans_tag")
@KeySequence("wx_account_fans_tag_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WxAccountFansTagDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Integer id;
    /**
     * 用户标识
     */
    private String openid;
    /**
     * 标签ID
     */
    private String tagId;
    /**
     * 微信账号ID
     */
    private String wxAccountId;

}
