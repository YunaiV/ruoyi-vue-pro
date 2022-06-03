package cn.iocoder.yudao.module.mp.dal.dataobject.mediaupload;

import lombok.*;

import java.util.*;

import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 微信素材上传表  DO
 *
 * @author 芋道源码
 */
@TableName("wx_media_upload")
@KeySequence("wx_media_upload_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WxMediaUploadDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Integer id;
    /**
     * 类型
     */
    private String type;
    /**
     * 图片URL
     */
    private String url;
    /**
     * 素材ID
     */
    private String mediaId;
    /**
     * 缩略图素材ID
     */
    private String thumbMediaId;
    /**
     * 微信账号ID
     */
    private String wxAccountId;

}
