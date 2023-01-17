package cn.iocoder.yudao.module.mp.dal.dataobject.tag;

import cn.iocoder.yudao.module.mp.dal.dataobject.account.MpAccountDO;
import lombok.*;

import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import me.chanjar.weixin.mp.bean.tag.WxUserTag;

/**
 * 公众号标签 DO
 *
 * @author 芋道源码
 */
@TableName("mp_tag")
@KeySequence("mp_tag_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MpTagDO extends BaseDO {

    /**
     * 主键
     */
    @TableId(type = IdType.INPUT)
    private Long id;
    /**
     * 公众号标签 id
     */
    private Long tagId;
    /**
     * 标签名
     */
    private String name;
    /**
     * 此标签下粉丝数
     *
     * 冗余：{@link WxUserTag#getCount()} 字段，需要管理员点击【同步】后，更新该字段
     */
    private Integer count;

    /**
     * 公众号账号的编号
     *
     * 关联 {@link MpAccountDO#getId()}
     */
    private Long accountId;
    /**
     * 公众号 appId
     *
     * 冗余 {@link MpAccountDO#getAppId()}
     */
    private String appId;

}
