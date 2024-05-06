package cn.iocoder.yudao.module.ai.dal.dataobject;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * ai 模型
 *
 * @author fansili
 * @time 2024/4/24 19:39
 * @since 1.0
 */
@Data
@Accessors(chain = true)
@TableName("ai_chat_modal")
public class AiChatModalDO extends BaseDO {

    /**
     * 编号
     */
    private Long id;
    /**
     * 名字
     */
    private String name;
    /**
     * 类型
     * {@link cn.iocoder.yudao.framework.ai.chatyiyan.YiYanChatModel}
     * {@link cn.iocoder.yudao.framework.ai.chatxinghuo.XingHuoChatModel}
     */
    private String modal;
    /**
     * 平台 参考：{@link cn.iocoder.yudao.framework.ai.AiPlatformEnum}
     */
    private String platform;
    /**
     * 图片地址
     */
    private String imageUrl;
    /**
     * 禁用 0、正常 1、禁用
     */
    private Integer disable;
    /**
     * 排序
     */
    private Integer sort;
    /**
     * modal 配置(json)
     */
    private String config;

}
