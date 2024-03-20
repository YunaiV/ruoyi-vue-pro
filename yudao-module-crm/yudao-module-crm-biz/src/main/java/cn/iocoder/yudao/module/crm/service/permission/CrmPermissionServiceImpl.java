package cn.iocoder.yudao.module.crm.service.permission;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.crm.controller.admin.permission.vo.CrmPermissionUpdateReqVO;
import cn.iocoder.yudao.module.crm.dal.dataobject.permission.CrmPermissionDO;
import cn.iocoder.yudao.module.crm.dal.mysql.permission.CrmPermissionMapper;
import cn.iocoder.yudao.module.crm.enums.common.CrmBizTypeEnum;
import cn.iocoder.yudao.module.crm.enums.permission.CrmPermissionLevelEnum;
import cn.iocoder.yudao.module.crm.service.permission.bo.CrmPermissionCreateReqBO;
import cn.iocoder.yudao.module.crm.service.permission.bo.CrmPermissionTransferReqBO;
import cn.iocoder.yudao.module.crm.util.CrmPermissionUtils;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.anyMatch;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.module.crm.enums.ErrorCodeConstants.*;
import static cn.iocoder.yudao.module.crm.enums.permission.CrmPermissionLevelEnum.isOwner;

/**
 * CRM 数据权限 Service 接口实现类
 *
 * @author HUIHUI
 */
@Service
@Validated
public class CrmPermissionServiceImpl implements CrmPermissionService {

    @Resource
    private CrmPermissionMapper permissionMapper;

    @Resource
    private AdminUserApi adminUserApi;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createPermission(CrmPermissionCreateReqBO createReqBO) {
        validatePermissionNotExists(Collections.singletonList(createReqBO));
        // 1. 校验用户是否存在
        adminUserApi.validateUserList(Collections.singletonList(createReqBO.getUserId()));

        // 2. 创建
        CrmPermissionDO permission = BeanUtils.toBean(createReqBO, CrmPermissionDO.class);
        permissionMapper.insert(permission);
        return permission.getId();
    }

    @Override
    public void createPermissionBatch(List<CrmPermissionCreateReqBO> createReqBOs) {
        validatePermissionNotExists(createReqBOs);
        // 1. 校验用户是否存在
        adminUserApi.validateUserList(convertSet(createReqBOs, CrmPermissionCreateReqBO::getUserId));

        // 2. 创建
        List<CrmPermissionDO> permissions = BeanUtils.toBean(createReqBOs, CrmPermissionDO.class);
        permissionMapper.insertBatch(permissions);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePermission(CrmPermissionUpdateReqVO updateReqVO) {
        // 1. 校验存在
        validatePermissionExists(updateReqVO.getIds());
        // 2. 更新
        List<CrmPermissionDO> updateList = CollectionUtils.convertList(updateReqVO.getIds(),
                id -> new CrmPermissionDO().setId(id).setLevel(updateReqVO.getLevel()));
        permissionMapper.updateBatch(updateList);
    }

    private void validatePermissionExists(Collection<Long> ids) {
        List<CrmPermissionDO> permissionList = permissionMapper.selectBatchIds(ids);
        if (ObjUtil.notEqual(permissionList.size(), ids.size())) {
            throw exception(CRM_PERMISSION_NOT_EXISTS);
        }
    }

    private void validatePermissionNotExists(Collection<CrmPermissionCreateReqBO> createReqBOs) {
        Set<Integer> bizTypes = convertSet(createReqBOs, CrmPermissionCreateReqBO::getBizType);
        Set<Long> bizIds = convertSet(createReqBOs, CrmPermissionCreateReqBO::getBizId);
        Set<Long> userIds = convertSet(createReqBOs, CrmPermissionCreateReqBO::getUserId);
        Long count = permissionMapper.selectListByBiz(bizTypes, bizIds, userIds);
        if (count > 0) {
            throw exception(CRM_PERMISSION_CREATE_FAIL);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void transferPermission(CrmPermissionTransferReqBO transferReqBO) {
        // 1. 校验数据权限：是否是负责人，只有负责人才可以转移
        CrmPermissionDO oldPermission = permissionMapper.selectByBizTypeAndBizIdByUserId(
                transferReqBO.getBizType(), transferReqBO.getBizId(), transferReqBO.getUserId());
        String bizTypeName = CrmBizTypeEnum.getNameByType(transferReqBO.getBizType());
        if (oldPermission == null // 不是拥有者，并且不是超管
                || (!isOwner(oldPermission.getLevel()) && !CrmPermissionUtils.isCrmAdmin())) {
            throw exception(CRM_PERMISSION_DENIED, bizTypeName);
        }
        // 1.1 校验转移对象是否已经是该负责人
        if (ObjUtil.equal(transferReqBO.getNewOwnerUserId(), oldPermission.getUserId())) {
            throw exception(CRM_PERMISSION_MODEL_TRANSFER_FAIL_OWNER_USER_EXISTS, bizTypeName);
        }
        // 1.2 校验新负责人是否存在
        adminUserApi.validateUserList(Collections.singletonList(transferReqBO.getNewOwnerUserId()));

        // 2. 修改新负责人的权限
        List<CrmPermissionDO> permissions = permissionMapper.selectByBizTypeAndBizId(
                transferReqBO.getBizType(), transferReqBO.getBizId()); // 获得所有数据权限
        CrmPermissionDO permission = CollUtil.findOne(permissions,
                item -> ObjUtil.equal(item.getUserId(), transferReqBO.getNewOwnerUserId()));
        if (permission == null) {
            permissionMapper.insert(new CrmPermissionDO().setBizType(transferReqBO.getBizType())
                    .setBizId(transferReqBO.getBizId()).setUserId(transferReqBO.getNewOwnerUserId())
                    .setLevel(CrmPermissionLevelEnum.OWNER.getLevel()));
        } else {
            permissionMapper.updateById(new CrmPermissionDO().setId(permission.getId())
                    .setLevel(CrmPermissionLevelEnum.OWNER.getLevel()));
        }

        // 3. 修改老负责人的权限
        if (transferReqBO.getOldOwnerPermissionLevel() != null) {
            permissionMapper.updateById(new CrmPermissionDO().setId(oldPermission.getId())
                    .setLevel(transferReqBO.getOldOwnerPermissionLevel()));
        } else {
            permissionMapper.deleteById(oldPermission.getId());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void transforPermissionBatch(List<CrmPermissionTransferReqBO> transferReqBOList){
        for (CrmPermissionTransferReqBO transferReqBO : transferReqBOList) {
            transferPermission(transferReqBO);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePermission(Integer bizType, Long bizId, Integer level) {
        // 校验存在
        List<CrmPermissionDO> permissions = permissionMapper.selectListByBizTypeAndBizIdAndLevel(
                bizType, bizId, level);
        if (CollUtil.isEmpty(permissions)) {
            throw exception(CRM_PERMISSION_NOT_EXISTS);
        }

        // 删除数据权限
        permissionMapper.deleteBatchIds(convertSet(permissions, CrmPermissionDO::getId));
    }

    @Override
    public void deletePermission(Integer bizType, Long bizId) {
        int deletedCount = permissionMapper.deletePermission(bizType, bizId);
        if (deletedCount == 0) {
            throw exception(CRM_PERMISSION_NOT_EXISTS);
        }
    }

    @Override
    public void deletePermissionBatch(Collection<Long> ids, Long userId) {
        List<CrmPermissionDO> permissions = permissionMapper.selectBatchIds(ids);
        if (CollUtil.isEmpty(permissions)) {
            throw exception(CRM_PERMISSION_NOT_EXISTS);
        }
        // 校验：数据权限的模块数据编号是一致的不可能存在两个
        if (convertSet(permissions, CrmPermissionDO::getBizId).size() > 1) {
            throw exception(CRM_PERMISSION_DELETE_FAIL);
        }
        // 校验操作人是否为负责人
        CrmPermissionDO permission = permissionMapper.selectByBizIdAndUserId(permissions.get(0).getBizId(), userId);
        if (permission == null) {
            throw exception(CRM_PERMISSION_DELETE_DENIED);
        }
        if (!CrmPermissionLevelEnum.isOwner(permission.getLevel())) {
            throw exception(CRM_PERMISSION_DELETE_DENIED);
        }

        // 删除数据权限
        permissionMapper.deleteBatchIds(ids);
    }

    @Override
    public void deleteSelfPermission(Long id, Long userId) {
        // 校验数据存在且是自己
        CrmPermissionDO permission = permissionMapper.selectByIdAndUserId(id, userId);
        if (permission == null) {
            throw exception(CRM_PERMISSION_NOT_EXISTS);
        }
        // 校验是否是负责人
        if (CrmPermissionLevelEnum.isOwner(permission.getLevel())) {
            throw exception(CRM_PERMISSION_DELETE_SELF_PERMISSION_FAIL_EXIST_OWNER);
        }

        // 删除
        permissionMapper.deleteById(id);
    }

    @Override
    public List<CrmPermissionDO> getPermissionListByBiz(Integer bizType, Long bizId) {
        return permissionMapper.selectByBizTypeAndBizId(bizType, bizId);
    }

    @Override
    public List<CrmPermissionDO> getPermissionListByBiz(Integer bizType, Collection<Long> bizIds) {
        return permissionMapper.selectByBizTypeAndBizIds(bizType, bizIds);
    }

    @Override
    public List<CrmPermissionDO> getPermissionListByBizTypeAndUserId(Integer bizType, Long userId) {
        return permissionMapper.selectListByBizTypeAndUserId(bizType, userId);
    }

    @Override
    public boolean hasPermission(Integer bizType, Long bizId, Long userId, CrmPermissionLevelEnum level) {
        List<CrmPermissionDO> permissionList = permissionMapper.selectByBizTypeAndBizId(bizType, bizId);
        return anyMatch(permissionList, permission ->
                ObjUtil.equal(permission.getUserId(), userId) && ObjUtil.equal(permission.getLevel(), level.getLevel()));
    }

}
