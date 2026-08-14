package cn.iocoder.yudao.module.hrm.dal.dataobject.recruit.config;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * HRM 招聘渠道 DO
 *
 * @author 芋道源码
 */
@TableName("hrm_recruit_channel")
@KeySequence("hrm_recruit_channel_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HrmRecruitChannelDO extends BaseDO {

    /**
     * 招聘渠道编号
     */
    @TableId
    private Long id;
    /**
     * 是否系统内置
     */
    private Boolean systemFlag;
    /**
     * 状态
     *
     * 枚举 {@link CommonStatusEnum}
     */
    private Integer status;
    /**
     * 渠道名称
     */
    private String name;
    /**
     * 显示顺序
     */
    private Integer sort;
    /**
     * 备注
     */
    private String remark;

}
