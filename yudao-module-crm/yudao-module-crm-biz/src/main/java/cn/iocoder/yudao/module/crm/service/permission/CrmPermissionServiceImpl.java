package cn.iocoder.yudao.module.crm.service.permission;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.module.crm.controller.admin.permission.vo.CrmPermissionUpdateReqVO;
import cn.iocoder.yudao.module.crm.convert.permission.CrmPermissionConvert;
import cn.iocoder.yudao.module.crm.dal.dataobject.permission.CrmPermissionDO;
import cn.iocoder.yudao.module.crm.dal.mysql.permission.CrmPermissionMapper;
import cn.iocoder.yudao.module.crm.framework.enums.CrmBizTypeEnum;
import cn.iocoder.yudao.module.crm.framework.enums.CrmPermissionLevelEnum;
import cn.iocoder.yudao.module.crm.service.permission.bo.CrmPermissionCreateReqBO;
import cn.iocoder.yudao.module.crm.service.permission.bo.CrmPermissionTransferReqBO;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.crm.enums.ErrorCodeConstants.*;
import static cn.iocoder.yudao.module.crm.framework.enums.CrmPermissionLevelEnum.isOwner;

// TODO @puhui999：尽量规避用“团队”这个词哈；这个只是我们给前端展示用的；
/**
 * CRM 数据权限 Service 接口实现类
 *
 * @author HUIHUI
 */
@Service
@Validated
public class CrmPermissionServiceImpl implements CrmPermissionService {

    @Resource
    private CrmPermissionMapper crmPermissionMapper;

    @Resource
    private AdminUserApi adminUserApi;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createPermission(CrmPermissionCreateReqBO createBO) {
        // 1. 校验用户是否存在
        adminUserApi.validateUserList(Collections.singletonList(createBO.getUserId()));

        // 2. 创建
        CrmPermissionDO permission = CrmPermissionConvert.INSTANCE.convert(createBO);
        crmPermissionMapper.insert(permission);
        return permission.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePermission(CrmPermissionUpdateReqVO updateReqVO) {
        // 校验存在
        validateCrmPermissionExists(updateReqVO.getIds());

        List<CrmPermissionDO> updateDO = CrmPermissionConvert.INSTANCE.convertList(updateReqVO);
        crmPermissionMapper.updateBatch(updateDO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePermission(Collection<Long> ids) {
        // 校验存在
        validateCrmPermissionExists(ids);

        // 删除
        crmPermissionMapper.deleteBatchIds(ids);
    }

    @Override
    public CrmPermissionDO getPermissionByBizTypeAndBizIdAndUserId(Integer bizType, Long bizId, Long userId) {
        return crmPermissionMapper.selectByBizTypeAndBizIdByUserId(bizType, bizId, userId);
    }

    @Override
    public CrmPermissionDO getPermissionByIdAndUserId(Long id, Long userId) {
        return crmPermissionMapper.selectByIdAndUserId(id, userId);
    }

    @Override
    public List<CrmPermissionDO> getPermissionByBizTypeAndBizId(Integer bizType, Long bizId) {
        return crmPermissionMapper.selectByBizTypeAndBizId(bizType, bizId);
    }

    @Override
    public List<CrmPermissionDO> getPermissionByBizTypeAndBizIdsAndLevel(Integer bizType, Collection<Long> bizIds, Integer level) {
        return crmPermissionMapper.selectListByBizTypeAndBizIdsAndLevel(bizType, bizIds, level);
    }

    private void validateCrmPermissionExists(Collection<Long> ids) {
        List<CrmPermissionDO> permissionList = crmPermissionMapper.selectBatchIds(ids);
        // 校验存在
        if (ObjUtil.notEqual(permissionList.size(), ids.size())) {
            throw exception(CRM_PERMISSION_NOT_EXISTS);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void transferPermission(CrmPermissionTransferReqBO transferReqBO) {
        // 1. 校验数据权限-是否是负责人，只有负责人才可以转移
        CrmPermissionDO oldPermission = crmPermissionMapper.selectByBizTypeAndBizIdByUserId(transferReqBO.getBizType(),
                transferReqBO.getBizId(), transferReqBO.getUserId());
        String crmName = CrmBizTypeEnum.getNameByType(transferReqBO.getBizType());
        // TODO 校验是否为超级管理员 || 1
        if (oldPermission == null || !isOwner(oldPermission.getLevel())) {
            throw exception(CRM_PERMISSION_DENIED, crmName);
        }
        // 1.1 校验转移对象是否已经是该负责人
        if (ObjUtil.equal(transferReqBO.getNewOwnerUserId(), oldPermission.getUserId())) {
            throw exception(CRM_PERMISSION_MODEL_TRANSFER_FAIL_OWNER_USER_EXISTS, crmName);
        }
        // 1.2 校验新负责人是否存在
        adminUserApi.validateUserList(Collections.singletonList(transferReqBO.getNewOwnerUserId()));

        // TODO @puhui999：2. 和 2.1 合并成 2；2.2 单独成 3；说白了，就是 2. 修改新负责人的权限；3. 修改老负责人的权限；这样整体注释会简洁一点，也清晰一点；
        // 2. 权限转移
        List<CrmPermissionDO> permissions = crmPermissionMapper.selectByBizTypeAndBizId(
                transferReqBO.getBizType(), transferReqBO.getBizId()); // 获取所有团队成员
        // 2.1 校验新负责人是否在团队成员中
        CrmPermissionDO permission = CollUtil.findOne(permissions,
                item -> ObjUtil.equal(item.getUserId(), transferReqBO.getNewOwnerUserId()));
        if (permission == null) { // 不存在则以负责人的级别加入这个团队
            crmPermissionMapper.insert(new CrmPermissionDO().setBizType(transferReqBO.getBizType())
                    .setBizId(transferReqBO.getBizId()).setUserId(transferReqBO.getNewOwnerUserId())
                    .setLevel(CrmPermissionLevelEnum.OWNER.getLevel()));
        } else { // 存在则修改权限级别
            crmPermissionMapper.updateById(new CrmPermissionDO().setId(permission.getId())
                    .setLevel(CrmPermissionLevelEnum.OWNER.getLevel()));
        }
        // 2.2. 老负责人处理
        if (transferReqBO.getOldOwnerPermissionLevel() != null) { // 加入团队
            crmPermissionMapper.updateById(new CrmPermissionDO().setId(oldPermission.getId())
                    .setLevel(transferReqBO.getOldOwnerPermissionLevel())); // 设置加入团队后的级别
            return;
        }
        crmPermissionMapper.deleteById(oldPermission.getId());
    }

    @Override
    public List<CrmPermissionDO> getPermissionListByBizTypeAndUserId(Integer bizType, Long userId) {
        return crmPermissionMapper.selectListByBizTypeAndUserId(bizType, userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void receiveBiz(Integer bizType, Long bizId, Long userId) {
        CrmPermissionDO permission = crmPermissionMapper.selectByBizTypeAndBizIdByUserId(bizType, bizId, CrmPermissionDO.POOL_USER_ID);
        if (permission == null) { // 不存在则模块数据也不存在
            throw exception(CRM_PERMISSION_MODEL_NOT_EXISTS, CrmBizTypeEnum.getNameByType(bizType));
        }

        crmPermissionMapper.updateById(new CrmPermissionDO().setId(permission.getId()).setUserId(userId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void putPool(Integer bizType, Long bizId, Long userId) {
        CrmPermissionDO permission = crmPermissionMapper.selectByBizTypeAndBizIdByUserId(bizType, bizId, userId);
        if (permission == null) { // 不存在则模块数据也不存在
            throw exception(CRM_PERMISSION_MODEL_NOT_EXISTS, CrmBizTypeEnum.getNameByType(bizType));
        }
        // 更新
        crmPermissionMapper.updateById(new CrmPermissionDO().setId(permission.getId()).setUserId(CrmPermissionDO.POOL_USER_ID));
    }

}
