package cn.iocoder.yudao.module.mp.dal.dataobject.material;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.mp.dal.dataobject.account.MpAccountDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;
import me.chanjar.weixin.common.api.WxConsts;

/**
 * 公众号素材 DO
 *
 * 1. <a href="https://developers.weixin.qq.com/doc/offiaccount/Asset_Management/New_temporary_materials.html">临时素材</a>
 * 2. <a href="https://developers.weixin.qq.com/doc/offiaccount/Asset_Management/Adding_Permanent_Assets.html">永久素材</a>
 *
 * @author 芋道源码
 */
@TableName("mp_material")
@KeySequence("mp_material_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MpMaterialDO extends BaseDO {

    /**
     * 主键
     */
    @TableId
    private Long id;
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

    /**
     * 公众号素材 id
     */
    private String mediaId;
    /**
     * 文件类型
     *
     * 枚举 {@link WxConsts.MediaFileType}
     */
    private String type;
    /**
     * 是否永久
     *
     * true - 永久素材
     * false - 临时素材
     */
    private Boolean permanent;
    /**
     * 文件服务器的 URL
     */
    private String url;

    /**
     * 名字
     *
     * 永久素材：非空
     * 临时素材：可能为空。
     *      1. 为空的情况：粉丝主动发送的图片、语音等
     *      2. 非空的情况：主动发送给粉丝的图片、语音等
     */
    private String name;

    /**
     * 公众号文件 URL
     *
     * 只有【永久素材】使用
     */
    private String mpUrl;

    /**
     * 视频素材的标题
     *
     * 只有【永久素材】使用
     */
    private String title;
    /**
     * 视频素材的描述
     *
     * 只有【永久素材】使用
     */
    private String introduction;

}
