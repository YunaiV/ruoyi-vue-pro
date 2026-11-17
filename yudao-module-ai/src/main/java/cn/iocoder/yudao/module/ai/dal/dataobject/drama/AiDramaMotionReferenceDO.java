package cn.iocoder.yudao.module.ai.dal.dataobject.drama;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * AI 短剧动作参考 DO：短剧项目内上传的动作参考视频素材
 *
 * @author 芋道源码
 */
@TableName("ai_drama_motion_reference")
@KeySequence("ai_drama_motion_reference_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
public class AiDramaMotionReferenceDO extends BaseDO {

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
     * 名称
     */
    private String name;
    /**
     * 视频地址（文件服务）：分镜选用时写入 {@link AiDramaStoryboardDO.Continuity#getReferenceVideoUrls()}
     */
    private String fileUrl;
    /**
     * 时长（秒）
     */
    private Double duration;

}
