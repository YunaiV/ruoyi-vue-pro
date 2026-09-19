package cn.iocoder.yudao.module.oa.service.file;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.oa.controller.admin.file.vo.permission.OaFilePermissionSaveReqVO;
import cn.iocoder.yudao.module.oa.dal.dataobject.file.OaFilePermissionDO;
import cn.iocoder.yudao.module.oa.dal.mysql.file.OaFilePermissionMapper;
import cn.iocoder.yudao.module.oa.enums.file.OaFilePermissionLevelEnum;
import cn.iocoder.yudao.module.oa.enums.file.OaFileSubjectTypeEnum;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.oa.enums.ErrorCodeConstants.*;

/**
 * 云盘共享权限 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Validated
public class OaFilePermissionServiceImpl implements OaFilePermissionService {

    @Resource
    private OaFilePermissionMapper filePermissionMapper;

    @Resource
    @Lazy // 延迟加载，避免循环依赖
    private OaFileNodeService fileNodeService;

    @Resource
    private AdminUserApi adminUserApi;
    @Resource
    private DeptApi deptApi;

    @Override
    public List<OaFilePermissionDO> getFilePermissionListByNodeIds(Collection<Long> nodeIds) {
        if (CollUtil.isEmpty(nodeIds)) {
            return Collections.emptyList();
        }
        return filePermissionMapper.selectListByNodeIds(nodeIds);
    }

    @Override
    public List<OaFilePermissionDO> getValidFilePermissionList(Long userId, Long deptId) {
        return filePermissionMapper.selectListByUserIdAndDeptIdAndExpireTimeAfter(userId, deptId, LocalDateTime.now());
    }

    @Override
    public List<OaFilePermissionDO> getFilePermissionList(Long nodeId, Long userId) {
        // 1. 校验节点管理权限
        fileNodeService.validateFileNodePermission(nodeId, userId, OaFilePermissionLevelEnum.MANAGE.getLevel());
        // 2. 查询节点共享权限
        return filePermissionMapper.selectListByNodeId(nodeId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveFilePermission(OaFilePermissionSaveReqVO reqVO, Long userId) {
        // 1.1 校验节点管理权限
        fileNodeService.lockFileNode(reqVO.getNodeId());
        fileNodeService.validateFileNodePermission(reqVO.getNodeId(), userId, OaFilePermissionLevelEnum.MANAGE.getLevel());
        // 1.2 校验共享主体确实存在
        if (ObjUtil.equal(reqVO.getSubjectType(), OaFileSubjectTypeEnum.USER.getType())) {
            adminUserApi.validateUser(reqVO.getSubjectId());
        } else if (ObjUtil.equal(reqVO.getSubjectType(), OaFileSubjectTypeEnum.DEPT.getType())) {
            deptApi.validateDeptList(Collections.singleton(reqVO.getSubjectId()));
        } else {
            throw new IllegalArgumentException("不支持的共享主体类型");
        }
        OaFilePermissionDO existing = filePermissionMapper.selectByNodeIdAndSubjectTypeAndSubjectId(
                reqVO.getNodeId(), reqVO.getSubjectType(), reqVO.getSubjectId());

        // 2.1 情况一：已有主体更新共享权限
        if (existing != null) {
            OaFilePermissionDO permission = BeanUtils.toBean(reqVO, OaFilePermissionDO.class).setId(existing.getId());
            filePermissionMapper.updatePermission(permission);
            return existing.getId();
        }

        // 2.2 情况二：新增主体共享权限
        OaFilePermissionDO permission = BeanUtils.toBean(reqVO, OaFilePermissionDO.class);
        filePermissionMapper.insert(permission);
        return permission.getId();
    }

    @Override
    public void deleteFilePermission(Long id, Long userId) {
        // 1.1 校验共享记录存在
        OaFilePermissionDO permission = filePermissionMapper.selectById(id);
        if (permission == null) {
            throw exception(FILE_PERMISSION_NOT_EXISTS);
        }
        // 1.2 校验节点管理权限
        fileNodeService.validateFileNodePermission(permission.getNodeId(), userId, OaFilePermissionLevelEnum.MANAGE.getLevel());

        // 2. 逻辑删除共享记录
        filePermissionMapper.deleteById(id);
    }

    @Override
    public void deleteFilePermissionsByNodeIds(Collection<Long> nodeIds) {
        if (CollUtil.isEmpty(nodeIds)) {
            return;
        }
        filePermissionMapper.deleteByNodeIds(nodeIds);
    }

}
