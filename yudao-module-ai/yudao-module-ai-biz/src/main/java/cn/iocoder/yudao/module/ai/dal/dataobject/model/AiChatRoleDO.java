package cn.iocoder.yudao.module.ai.dal.dataobject.model;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * ai 聊天角色
 *
 * @fansili
 * @since v1.0
 */
@Data
@Accessors(chain = true)
@TableName("ai_chat_role")
public class AiChatRoleDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 角色名称
     */
    private String name;
    /**
     * 角色头像
     */
    private String avatar;
    /**
     * 角色分类
     */
    private String category;
    /**
     * 角色描述
     */
    private String description;
    /**
     * 角色欢迎语
     */
    private String welcomeMessage;

    /**
     * 用户编号
     *
     * 关联 AdminUserDO 的 userId 字段
     */
    private Long userId;

    /**
     * 模型编号
     *
     * 关联 {@link AiChatModalDO#getId()} 字段
     */
    private String modelId;

    /**
     * 是否公开
     *
     * true - 公开；false - 私有
     */
    private Boolean publicStatus;

    /**
     * 排序值
     */
    private Integer sort;
    /**
     * 状态
     *
     * 枚举 {@link CommonStatusEnum}
     */
    private Integer status;

    // TODO 芋艿：要不要加一个 context，内置的上下文

}
