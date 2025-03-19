package cn.iocoder.yudao.module.bpm.dal.dataobject.definition;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * BPM 流程分类 DO
 *
 * @author 芋道源码
 */
@TableName("bpm_category")
@KeySequence("bpm_category_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BpmCategoryDO extends BaseDO {

    /**
     * 分类编号
     */
    @TableId
    private Long id;
    /**
     * 分类名
     */
    private String name;
    /**
     * 分类标志
     */
    private String code;
    /**
     * 分类描述
     */
    private String description;
    /**
     * 分类状态
     *
     * 枚举 {@link cn.iocoder.yudao.framework.common.enums.CommonStatusEnum}
     */
    private Integer status;
    /**
     * 分类排序
     */
    private Integer sort;

}