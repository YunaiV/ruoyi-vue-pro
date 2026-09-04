package cn.iocoder.yudao.module.pms.dal.dataobject.pm.workitem;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * PMS 工作项标签 DO
 *
 * @author 芋道源码
 */
@TableName("pms_work_item_label")
@KeySequence("pms_work_item_label_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PmsWorkItemLabelDO extends BaseDO {

    /**
     * 标签编号
     */
    @TableId
    private Long id;
    /**
     * 标签名称
     */
    private String name;
    /**
     * 标签颜色
     */
    private String color;

}
