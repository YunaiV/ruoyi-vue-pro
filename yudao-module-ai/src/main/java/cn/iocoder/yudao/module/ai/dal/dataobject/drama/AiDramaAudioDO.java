package cn.iocoder.yudao.module.ai.dal.dataobject.drama;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import cn.iocoder.yudao.module.ai.enums.drama.AiDramaAudioTypeEnum;

/**
 * AI 短剧音频 DO：角色样音、旁白
 *
 * @author 芋道源码
 */
@TableName("ai_drama_audio")
@KeySequence("ai_drama_audio_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
public class AiDramaAudioDO extends BaseDO {

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
     * 角色编号：样音绑定的角色；旁白为空
     * <p>
     * 关联 {@link AiDramaCharacterDO#getId()}
     */
    private Long characterId;
    /**
     * 类型
     * <p>
     * 枚举 {@link AiDramaAudioTypeEnum}
     */
    private Integer type;
    /**
     * 文件地址（文件服务）
     */
    private String fileUrl;

}
