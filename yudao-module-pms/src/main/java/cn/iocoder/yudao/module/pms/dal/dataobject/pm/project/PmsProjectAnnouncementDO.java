package cn.iocoder.yudao.module.pms.dal.dataobject.pm.project;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

/**
 * PMS 项目公告 DO
 *
 * @author 芋道源码
 */
@TableName(value = "pms_project_announcement", autoResultMap = true)
@KeySequence("pms_project_announcement_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PmsProjectAnnouncementDO extends BaseDO {

    /**
     * 公告编号
     */
    @TableId
    private Long id;
    /**
     * 项目编号
     *
     * 关联 {@link PmsProjectDO#getId()}
     */
    private Long projectId;
    /**
     * 公告内容
     */
    private String content;
    /**
     * 附件地址列表
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> fileUrls;

}
