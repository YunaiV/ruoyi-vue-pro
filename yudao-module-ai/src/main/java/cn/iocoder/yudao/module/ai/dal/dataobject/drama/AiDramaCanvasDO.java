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
 * AI 短剧画布 DO：短剧项目内的流程画布，每个项目一份
 *
 * @author 芋道源码
 */
@TableName(value = "ai_drama_canvas", autoResultMap = true)
@KeySequence("ai_drama_canvas_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
public class AiDramaCanvasDO extends BaseDO {

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
     * 修订号：每次保存加 1，用于避免多个窗口互相覆盖
     */
    private Long revision;
    /**
     * 画布文档：节点、连线等，JSON 对象
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> document;

}
