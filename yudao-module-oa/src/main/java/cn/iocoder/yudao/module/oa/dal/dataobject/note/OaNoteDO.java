package cn.iocoder.yudao.module.oa.dal.dataobject.note;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.oa.enums.note.OaNoteTypeEnum;
import cn.iocoder.yudao.module.oa.enums.schedule.OaPriorityEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.Jackson3TypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

/**
 * OA 笔记 DO
 *
 * @author 芋道源码
 */
@TableName(value = "oa_note", autoResultMap = true)
@KeySequence("oa_note_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class OaNoteDO extends BaseDO {

    /**
     * 笔记编号
     */
    @TableId
    private Long id;
    /**
     * 目录编号
     *
     * 关联 {@link OaNoteCategoryDO#getId()}
     */
    private Long categoryId;
    /**
     * 笔记类型
     *
     * 枚举 {@link OaNoteTypeEnum}
     */
    private Integer type;
    /**
     * 优先级
     *
     * 枚举 {@link OaPriorityEnum}
     */
    private Integer priority;
    /**
     * 标题
     */
    private String title;
    /**
     * 内容
     */
    private String content;
    /**
     * 是否收藏
     */
    private Boolean favorite;
    /**
     * 附件地址列表
     */
    @TableField(typeHandler = Jackson3TypeHandler.class)
    private List<String> fileUrls;

}
