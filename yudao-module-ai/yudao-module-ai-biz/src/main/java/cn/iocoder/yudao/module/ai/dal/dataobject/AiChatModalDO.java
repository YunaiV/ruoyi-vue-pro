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
     * 模型key
     */
    private String modelKey;
    /**
     * 模型类型 参考：{@link cn.iocoder.yudao.framework.ai.AiPlatformEnum}
     */
    private String modelPlatform;
    /**
     * 模型类型
     * {@link cn.iocoder.yudao.framework.ai.chatyiyan.YiYanChatModel}
     * {@link cn.iocoder.yudao.framework.ai.chatxinghuo.XingHuoChatModel}
     */
    private String modelType;
    /**
     * 模型名字
     */
    private String modelName;
    /**
     * 模型照片
     */
    private String modalImage;
    /**
     * 禁用 0、正常 1、禁用
     */
    private Integer disable;
    /**
     * modal 配置(json)
     */
    private String config;
}
