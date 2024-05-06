package cn.iocoder.yudao.module.ai.dal.dataobject;

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
     * 编号，表示聊天角色在数据库中的唯一标识符
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID，关联到使用该聊天角色的用户
     */
    private Long userId;

    /**
     * 角色名，角色的显示名称
     */
    private String name;

    /**
     * 角色介绍，详细描述角色的功能或用途
     */
    private String introduce;

    /**
     * 分类，角色所属的类别，如娱乐、创作等
     */
    private String classify;

    /**
     * 是否开启 open、close
     */
    private String enable;

    /**
     * 角色的使用次数统计
     */
    private Integer useCount;

}
