package cn.iocoder.yudao.module.oa.dal.dataobject.seal;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.oa.enums.DictTypeConstants;
import cn.iocoder.yudao.module.oa.enums.seal.OaSealStatusEnum;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * OA 印章 DO
 *
 * @author 芋道源码
 */
@TableName(value = "oa_seal", autoResultMap = true)
@KeySequence("oa_seal_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class OaSealDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 所属部门编号
     *
     * 关联 {@link DeptRespDTO#getId()}，与保管部门独立维护
     */
    private Long deptId;
    /**
     * 印章编号
     */
    private String no;
    /**
     * 印章名称
     */
    private String name;
    /**
     * 印章类型
     *
     * 字典 {@link DictTypeConstants#SEAL_TYPE}
     */
    private Integer type;
    /**
     * 印章分类
     *
     * 字典 {@link DictTypeConstants#SEAL_CATEGORY}
     */
    private Integer category;
    /**
     * 保管人用户编号
     *
     * 关联 {@link AdminUserRespDTO#getId()}
     */
    private Long keeperUserId;
    /**
     * 保管部门编号
     *
     * 关联 {@link DeptRespDTO#getId()}
     */
    private Long keeperDeptId;
    /**
     * 印章台账状态
     *
     * 枚举 {@link OaSealStatusEnum}
     *
     * 字典 {@link DictTypeConstants#SEAL_STATUS}
     */
    private Integer status;
    /**
     * 购买时间
     */
    private LocalDateTime purchaseTime;
    /**
     * 启用时间
     */
    private LocalDateTime enableTime;
    /**
     * 停用时间
     */
    private LocalDateTime disableTime;
    /**
     * 印章照片地址
     */
    private String picUrl;
    /**
     * 显示顺序
     */
    private Integer sort;
    /**
     * 备注
     */
    private String remark;

}
