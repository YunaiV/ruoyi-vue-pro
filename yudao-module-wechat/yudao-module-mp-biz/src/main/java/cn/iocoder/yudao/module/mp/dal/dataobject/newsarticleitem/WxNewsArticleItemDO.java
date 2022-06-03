package cn.iocoder.yudao.module.mp.dal.dataobject.newsarticleitem;

import lombok.*;

import java.util.*;

import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 图文消息文章列表表  DO
 *
 * @author 芋道源码
 */
@TableName("wx_news_article_item")
@KeySequence("wx_news_article_item_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WxNewsArticleItemDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Integer id;
    /**
     * 标题
     */
    private String title;
    /**
     * 摘要
     */
    private String digest;
    /**
     * 作者
     */
    private String author;
    /**
     * 是否展示封面图片（0/1）
     */
    private String showCoverPic;
    /**
     * 上传微信，封面图片标识
     */
    private String thumbMediaId;
    /**
     * 内容
     */
    private String content;
    /**
     * 内容链接
     */
    private String contentSourceUrl;
    /**
     * 文章排序
     */
    private Integer orderNo;
    /**
     * 图片路径
     */
    private String picPath;
    /**
     * 是否可以留言
     */
    private String needOpenComment;
    /**
     * 是否仅粉丝可以留言
     */
    private String onlyFansCanComment;
    /**
     * 图文ID
     */
    private String newsId;
    /**
     * 微信账号ID
     */
    private String wxAccountId;

}
