package cn.iocoder.dashboard.modules.system.dal.mysql.dataobject.dept;

import cn.iocoder.dashboard.common.enums.CommonStatusEnum;
import cn.iocoder.dashboard.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 岗位表
 *
 * @author ruoyi
 */
@TableName("sys_post")
@Data
@EqualsAndHashCode(callSuper = true)
public class SysPostDO extends BaseDO {

    /**
     * 岗位序号
     */
    @TableId
    private Long id;

    /**
     * 岗位名称
     */
    private String name;
    /**
     * 岗位编码
     */
    private String code;
    /**
     * 岗位排序
     */
    private String sort;
    /**
     * 状态
     *
     * 枚举 {@link CommonStatusEnum}
     */
    private String status;
    /**
     * 备注
     */
    private String remark;

}
