package cn.iocoder.yudao.module.oa.dal.dataobject.file;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import cn.iocoder.yudao.module.oa.enums.file.OaFilePermissionLevelEnum;
import cn.iocoder.yudao.module.oa.enums.file.OaFileSubjectTypeEnum;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * OA 云盘共享权限 DO
 *
 * @author 芋道源码
 */
@TableName(value = "oa_file_permission", autoResultMap = true)
@KeySequence("oa_file_permission_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class OaFilePermissionDO extends BaseDO {

    /**
     * 编号
     */
    @TableId
    private Long id;
    /**
     * 共享节点编号
     *
     * 关联 {@link OaFileNodeDO#getId()}
     */
    private Long nodeId;
    /**
     * 共享主体类型
     *
     * 枚举 {@link OaFileSubjectTypeEnum}
     */
    private Integer subjectType;
    /**
     * 共享主体编号
     *
     * 用户主体关联 {@link cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO#getId()}
     * 部门主体关联 {@link cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO#getId()}
     */
    private Long subjectId;
    /**
     * 共享权限级别
     *
     * 枚举 {@link OaFilePermissionLevelEnum}
     */
    private Integer level;
    /**
     * 是否继承到子目录
     */
    private Boolean inherit;
    /**
     * 权限到期时间
     */
    private LocalDateTime expireTime;

}
