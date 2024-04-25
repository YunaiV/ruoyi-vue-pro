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
     * 模型名字
     */
    private Long modelName;
    /**
     * 模型类型(qianwen、yiyan、xinghuo、openai)
     */
    private String modelType;
    /**
     * 模型照片
     */
    private String modalImage;
    /**
     * 禁用 0、正常 1、禁用
     */
    private Integer disable;

}
