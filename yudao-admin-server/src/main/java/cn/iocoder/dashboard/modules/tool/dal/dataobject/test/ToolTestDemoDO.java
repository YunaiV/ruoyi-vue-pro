package cn.iocoder.dashboard.modules.tool.dal.dataobject.test;

import cn.iocoder.dashboard.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

/**
* 字典类型 DO
*
* @author 芋艿
*/
@TableName("tool_test_demo")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToolTestDemoDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 名字
     */
    private String name;
    /**
     * 状态
     *
     * 枚举 {@link cn.iocoder.yudao.common.enums.CommonStatusEnum}
     */
    private Integer status;
    /**
     * 类型
     *
     * 枚举 {@link cn.iocoder.dashboard.framework.logger.operatelog.core.enums.OperateTypeEnum}
     */
    private Integer type;
    /**
     * 分类
     *
     * 枚举 {@link cn.iocoder.dashboard.framework.redis.core.RedisKeyDefine.TimeoutTypeEnum}
     */
    private Integer category;
    /**
     * 备注
     */
    private String remark;

}
