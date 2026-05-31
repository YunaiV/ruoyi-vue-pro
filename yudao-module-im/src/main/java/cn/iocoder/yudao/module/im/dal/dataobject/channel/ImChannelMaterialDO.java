package cn.iocoder.yudao.module.im.dal.dataobject.channel;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.im.dal.dataobject.message.ImChannelMessageDO;
import cn.iocoder.yudao.module.im.enums.channel.ImChannelMaterialTypeEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
 * IM 频道素材 DO
 * <p>
 * 业务语义：
 * - 运营素材库，可被反复推送
 * - 一条素材 1:N 关联多条 {@link ImChannelMessageDO}
 * - {@link #content} 富文本仅在素材详情接口按需返回，推送 payload 不带，避免压爆 WebSocket 通道
 *
 * @author 芋道源码
 */
@TableName("im_channel_material")
@KeySequence("im_channel_material_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImChannelMaterialDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 频道编号
     * <p>
     * 关联 {@link ImChannelDO#getId()}
     */
    private Long channelId;
    /**
     * 素材内容类型
     * <p>
     * 枚举 {@link ImChannelMaterialTypeEnum}
     */
    private Integer type;
    /**
     * 标题
     */
    private String title;
    /**
     * 封面图
     */
    private String coverUrl;
    /**
     * 摘要
     */
    private String summary;
    /**
     * 富文本 HTML；在 {@link ImChannelMaterialTypeEnum#CONTENT} 使用
     */
    private String content;
    /**
     * 跳转链接；在 {@link ImChannelMaterialTypeEnum#LINK} 使用
     */
    private String url;

}
