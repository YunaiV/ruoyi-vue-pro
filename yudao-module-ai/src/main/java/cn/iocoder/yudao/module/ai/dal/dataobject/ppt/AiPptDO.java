package cn.iocoder.yudao.module.ai.dal.dataobject.ppt;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiModelDO;
import cn.iocoder.yudao.module.ai.enums.ppt.AiPptModeEnum;
import cn.iocoder.yudao.module.ai.enums.ppt.AiPptSourceEnum;
import cn.iocoder.yudao.module.ai.enums.ppt.AiPptStatusEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * AI PPT DO
 *
 * 一套 PPT 一行：分镜、每页的当前图与历史版本，都放在 {@link #slides} JSON 里。
 * 每页是一张整页图，标题、要点由绘图模型画在图里，导出的 PPTX、PDF 也是每页一张图
 *
 * @author 芋道源码
 */
@TableName(value = "ai_ppt", autoResultMap = true)
@KeySequence("ai_ppt_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
public class AiPptDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 用户编号
     * <p>
     * 关联 AdminUserDO 的 userId 字段
     */
    private Long userId;

    /**
     * 标题
     */
    private String title;
    /**
     * 素材来源
     * <p>
     * 枚举 {@link AiPptSourceEnum}
     */
    private Integer source;
    /**
     * 大纲或材料文本
     */
    private String content;
    /**
     * 上传材料文件地址
     */
    private String fileUrl;
    /**
     * 风格与设计要求
     */
    private String prompt;
    /**
     * 格式
     * <p>
     * 枚举 {@link AiPptModeEnum}
     */
    private Integer mode;
    /**
     * 语言：zh-CN、en
     */
    private String language;
    /**
     * 请求页数
     */
    private Integer pageCount;
    /**
     * 对话模型编号（生成分镜）
     * <p>
     * 关联 {@link AiModelDO#getId()}
     */
    private Long chatModelId;
    /**
     * 绘图模型编号（整页图）
     * <p>
     * 关联 {@link AiModelDO#getId()}
     */
    private Long imageModelId;

    /**
     * 分镜与配图
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Slide> slides;
    /**
     * 封面图地址
     * <p>
     * 冗余 {@link #slides} 第一张有图的页的当前图，用于列表展示
     */
    private String coverUrl;
    /**
     * 分镜页数，0 表示还在分镜阶段
     * <p>
     * 冗余 {@link #slides} 的数量，用于列表展示进度
     */
    private Integer slideCount;
    /**
     * 已有配图页数
     * <p>
     * 冗余 {@link #slides} 中有图的数量，用于列表展示进度
     */
    private Integer finishCount;

    /**
     * 状态
     * <p>
     * 枚举 {@link AiPptStatusEnum}
     */
    private Integer status;
    /**
     * 生成轮次：每次进入生成中（新建、继续生成、单页重绘）加 1；切换版本也加 1
     * <p>
     * 异步执行者记住开始时的轮次，只在任务仍为生成中、且轮次未变时写入；
     * 超时后继续生成、再次重绘时，上一轮迟到的分镜、图片、失败结果都会被丢弃，不会覆盖或结束新一轮。
     * 用户的修改（继续生成、重绘、切换版本）都会加 1，基于旧数据的并发修改会失败，不会互相覆盖
     */
    private Integer attempt;
    /**
     * 失败原因
     */
    private String errorMessage;
    /**
     * 完成时间
     */
    private LocalDateTime finishTime;

    /**
     * 幻灯片（一页）
     */
    @Data
    public static class Slide {

        /**
         * 页码，从 1 开始
         */
        private Integer index;
        /**
         * 本页主题
         */
        private String theme;
        /**
         * 本页标题
         */
        private String title;
        /**
         * 本页要点
         */
        private List<String> points;
        /**
         * 首次生图的提示词
         */
        private String imagePrompt;
        /**
         * 当前图地址，为空表示本页还没有图
         */
        private String imageUrl;
        /**
         * 本页最近一次生成（首次或重绘）的状态，为空表示待生成
         * <p>
         * 枚举 {@link AiPptStatusEnum}
         */
        private Integer status;
        /**
         * 本页最近一次失败的原因
         */
        private String errorMessage;
        /**
         * 历史版本：所有成功生成过的图，按时间追加
         */
        private List<SlideImage> images;

    }

    /**
     * 幻灯片的一个图片版本
     */
    @Data
    public static class SlideImage {

        /**
         * 图片地址
         */
        private String url;
        /**
         * 实际发给绘图模型的完整提示词
         */
        private String prompt;
        /**
         * 生成时间
         */
        private LocalDateTime createTime;

    }

}
