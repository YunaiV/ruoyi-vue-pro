package cn.iocoder.yudao.module.ai.dal.dataobject;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
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
public class AiChatRoleDO extends BaseDO {
    /**
     * 编号，表示聊天角色在数据库中的唯一标识符
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID，关联到使用该聊天角色的用户
     */
    private Long userId;

    /**
     * 模型编号，关联到角色使用的特定模型
     */
    private String modelId;

    /**
     * 角色名，角色的显示名称
     */
    private String roleName;

    /**
     * 角色介绍，详细描述角色的功能或用途
     */
    private String roleIntroduce;

    /**
     * 角色来源，如 system（系统预置）、customer（用户自定义）
     */
    private String roleSource;

    /**
     * 分类，角色所属的类别，如娱乐、创作等
     */
    private String classify;

    /**
     * 发布状态，0表示仅自己可见，1表示公开，2表示禁用
     */
    private String visibility;

    /**
     * 生成时的Top-K采样候选集大小
     */
    private Double topK;

    /**
     * 生成时使用的核采样方法的概率阈值
     */
    private Double topP;

    /**
     * 用于控制随机性和多样性的温度参数
     */
    private Double temperature;

    /**
     * 角色的使用次数统计
     */
    private Integer useCount;

}
