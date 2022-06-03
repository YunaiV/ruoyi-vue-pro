package cn.iocoder.yudao.module.mp.dal.dataobject.fanstag;

import lombok.*;

import java.util.*;

import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 粉丝标签 DO
 *
 * @author 芋道源码
 */
@TableName("wx_fans_tag")
@KeySequence("wx_fans_tag_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WxFansTagDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Integer id;
    /**
     * 标签名称
     */
    private String name;
    /**
     * 粉丝数量
     */
    private Integer count;
    /**
     * 微信账号ID
     */
    private String wxAccountId;

}
