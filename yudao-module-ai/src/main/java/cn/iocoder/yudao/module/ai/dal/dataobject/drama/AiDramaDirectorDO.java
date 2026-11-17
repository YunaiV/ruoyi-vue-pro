package cn.iocoder.yudao.module.ai.dal.dataobject.drama;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.util.Map;

/**
 * AI 短剧导演台 DO：短剧项目画布中预演节点对应的导演台文档，每个节点一份
 *
 * @author 芋道源码
 */
@TableName(value = "ai_drama_director", autoResultMap = true)
@KeySequence("ai_drama_director_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
public class AiDramaDirectorDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 项目编号
     * <p>
     * 关联 {@link AiDramaProjectDO#getId()}
     */
    private Long projectId;
    /**
     * 画布节点编号：对应画布中预演节点的 id
     * <p>
     * 关联 {@link AiDramaCanvasDO#getDocument()} 中的节点
     */
    private String nodeId;
    /**
     * 修订号：每次保存加 1，用于避免多个窗口互相覆盖
     */
    private Long revision;
    /**
     * 导演台文档：场景、机位、角色走位等，JSON 对象
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> document;

}
