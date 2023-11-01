package cn.iocoder.yudao.module.crm.service.permission;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.crm.convert.permission.CrmPermissionConvert;
import cn.iocoder.yudao.module.crm.dal.dataobject.permission.CrmPermissionDO;
import cn.iocoder.yudao.module.crm.dal.mysql.permission.CrmPermissionMapper;
import cn.iocoder.yudao.module.crm.framework.enums.CrmBizTypeEnum;
import cn.iocoder.yudao.module.crm.framework.enums.CrmPermissionLevelEnum;
import cn.iocoder.yudao.module.crm.service.permission.bo.CrmPermissionCreateReqBO;
import cn.iocoder.yudao.module.crm.service.permission.bo.CrmPermissionPageReqBO;
import cn.iocoder.yudao.module.crm.service.permission.bo.CrmPermissionUpdateReqBO;
import cn.iocoder.yudao.module.crm.service.permission.bo.CrmTransferPermissionReqBO;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.crm.enums.ErrorCodeConstants.*;
import static cn.iocoder.yudao.module.crm.framework.enums.CrmPermissionLevelEnum.isOwner;

/**
 * crm 数据权限 Service 接口实现类
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
        CrmPermissionDO permission = CrmPermissionConvert.INSTANCE.convert(createBO);
        crmPermissionMapper.insert(permission);
        return permission.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePermission(CrmPermissionUpdateReqBO updateBO) {
        validateCrmPermissionExists(updateBO.getId());
        // 更新操作
        CrmPermissionDO updateDO = CrmPermissionConvert.INSTANCE.convert(updateBO);
        crmPermissionMapper.updateById(updateDO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePermission(Long id) {
        validateCrmPermissionExists(id);
        // 删除
        crmPermissionMapper.deleteById(id);
    }

    @Override
    public CrmPermissionDO getPermissionByBizTypeAndBizIdAndUserId(Integer bizType, Long bizId, Long userId) {
        return crmPermissionMapper.selectByBizTypeAndBizIdByUserId(bizType, bizId, userId);
    }

    @Override
    public List<CrmPermissionDO> getPermissionByBizTypeAndBizId(Integer bizType, Long bizId) {
        return crmPermissionMapper.selectByBizTypeAndBizId(bizType, bizId);
    }

    @Override
    public List<CrmPermissionDO> getPermissionByBizTypeAndUserId(Integer bizType, Long userId) {
        return crmPermissionMapper.selectByBizTypeAndUserId(bizType, userId);
    }

    private void validateCrmPermissionExists(Long id) {
        if (crmPermissionMapper.selectById(id) == null) {
            throw exception(CRM_PERMISSION_NOT_EXISTS);
        }
    }


    @Override
    public void transferPermission(CrmTransferPermissionReqBO transferReqBO) {
        // 1. 校验数据权限-是否是负责人，只有负责人才可以转移
        CrmPermissionDO oldPermission = crmPermissionMapper.selectByBizTypeAndBizIdByUserId(transferReqBO.getBizType(),
                transferReqBO.getBizId(), transferReqBO.getUserId());
        String crmName = CrmBizTypeEnum.getNameByType(transferReqBO.getBizType());
        // TODO 校验是否为超级管理员 || 1
        if (oldPermission == null || !isOwner(oldPermission.getPermissionLevel())) {
            throw exception(CRM_PERMISSION_DENIED, crmName);
        }

        // 2. 校验转移对象是否已经是该负责人
        if (ObjUtil.equal(transferReqBO.getNewOwnerUserId(), oldPermission.getUserId())) {
            throw exception(CRM_PERMISSION_MODEL_TRANSFER_FAIL_OWNER_USER_EXISTS, crmName);
        }
        // 2.1 校验新负责人是否存在
        AdminUserRespDTO user = adminUserApi.getUser(transferReqBO.getNewOwnerUserId());
        if (user == null) {
            throw exception(CRM_PERMISSION_MODEL_TRANSFER_FAIL_OWNER_USER_NOT_EXISTS, crmName);
        }

        // 3. 权限转移
        List<CrmPermissionDO> permissions = crmPermissionMapper.selectByBizTypeAndBizId(
                transferReqBO.getBizType(), transferReqBO.getBizId()); // 获取所有团队成员
        // 3.1 校验新负责人是否在团队成员中
        CrmPermissionDO permission = CollUtil.findOne(permissions,
                item -> ObjUtil.equal(item.getUserId(), transferReqBO.getNewOwnerUserId()));
        if (permission == null) { // 不存在则以负责人的级别加入这个团队
            crmPermissionMapper.insert(new CrmPermissionDO().setBizType(transferReqBO.getBizType())
                    .setBizId(transferReqBO.getBizId()).setUserId(transferReqBO.getNewOwnerUserId())
                    .setPermissionLevel(CrmPermissionLevelEnum.OWNER.getLevel()));

        } else { // 存在则修改权限级别
            crmPermissionMapper.updateById(new CrmPermissionDO().setId(permission.getId())
                    .setPermissionLevel(CrmPermissionLevelEnum.OWNER.getLevel()));
        }

        // 4. 老负责人处理
        if (transferReqBO.getJoinTeam()) { // 加入团队
            crmPermissionMapper.updateById(new CrmPermissionDO().setId(oldPermission.getId())
                    .setPermissionLevel(transferReqBO.getPermissionLevel())); // 设置加入团队后的级别
            return;
        }
        crmPermissionMapper.deleteById(oldPermission.getId()); // 移除
    }

    @Override
    public PageResult<CrmPermissionDO> getPermissionPage(CrmPermissionPageReqBO pageReqBO) {
        return crmPermissionMapper.selectPage(pageReqBO);
    }

}
