package cn.iocoder.yudao.module.ai.dal.dataobject.drama;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * AI 短剧剧本 DO
 *
 * 同一项目可有多条，最新一条为当前剧本
 *
 * @author 芋道源码
 */
@TableName(value = "ai_drama_script", autoResultMap = true)
@KeySequence("ai_drama_script_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
public class AiDramaScriptDO extends BaseDO {

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
     * 剧本标题
     */
    private String title;
    /**
     * 叙事基调
     */
    private String tone;
    /**
     * 剧情大纲：公开作品作为剧情简介
     */
    private String outline;
    /**
     * 剧本正文
     */
    private String content;
    /**
     * 打磨意见与修改记录
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<RevisionNote> revisionNotes;

    /**
     * 打磨意见
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RevisionNote {

        /**
         * 意见
         */
        private String note;
        // DONE @AI：localdatetime 把？！（已改为 LocalDateTime；JSON 里存毫秒时间戳，历史的 yyyy-MM-dd HH:mm:ss 字符串也能读取）
        /**
         * 时间
         */
        private LocalDateTime time;

    }

}
