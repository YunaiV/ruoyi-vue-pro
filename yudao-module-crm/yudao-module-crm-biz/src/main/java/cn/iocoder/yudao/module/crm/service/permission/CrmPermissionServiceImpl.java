package cn.iocoder.yudao.module.crm.service.permission;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.module.crm.convert.permission.CrmPermissionConvert;
import cn.iocoder.yudao.module.crm.dal.dataobject.permission.CrmPermissionDO;
import cn.iocoder.yudao.module.crm.dal.mysql.permission.CrmPermissionMapper;
import cn.iocoder.yudao.module.crm.enums.common.PermissionTypeEnum;
import cn.iocoder.yudao.module.crm.enums.common.TransferTypeEnum;
import cn.iocoder.yudao.module.crm.framework.enums.CrmEnum;
import cn.iocoder.yudao.module.crm.service.permission.bo.CrmPermissionCreateBO;
import cn.iocoder.yudao.module.crm.service.permission.bo.CrmPermissionUpdateBO;
import cn.iocoder.yudao.module.crm.service.permission.bo.TransferCrmPermissionBO;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Set;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.crm.enums.ErrorCodeConstants.*;

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
    public Long createCrmPermission(CrmPermissionCreateBO createBO) {
        CrmPermissionDO createDO = CrmPermissionConvert.INSTANCE.convert(createBO);
        crmPermissionMapper.insert(createDO);
        return createDO.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCrmPermission(CrmPermissionUpdateBO updateBO) {
        validateCrmPermissionExists(updateBO.getId());

        CrmPermissionDO updateDO = CrmPermissionConvert.INSTANCE.convert(updateBO);
        crmPermissionMapper.updateById(updateDO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCrmPermission(Long id) {
        validateCrmPermissionExists(id);

        crmPermissionMapper.deleteById(id);
    }

    private void validateCrmPermissionExists(Long id) {
        if (crmPermissionMapper.selectById(id) == null) {
            throw exception(CRM_PERMISSION_NOT_EXISTS);
        }
    }

    @Override
    public CrmPermissionDO getCrmPermissionByCrmTypeAndCrmDataId(Integer crmType, Long crmDataId) {
        return crmPermissionMapper.selectByCrmTypeAndCrmDataId(crmType, crmDataId);
    }

    @Override
    public void transferCrmPermission(TransferCrmPermissionBO transferCrmPermissionBO) {
        // 1 校验商机是否存在
        CrmPermissionDO permission = getCrmPermissionByCrmTypeAndCrmDataId(transferCrmPermissionBO.getCrmType(),
                transferCrmPermissionBO.getCrmDataId());
        String crmName = CrmEnum.getNameByType(transferCrmPermissionBO.getCrmType());
        if (permission == null) {
            throw exception(CRM_PERMISSION_MODEL_NOT_EXISTS, crmName);
        }
        // 1.2 校验转移对象是否已经是该负责人
        if (ObjUtil.equal(permission.getOwnerUserId(), permission.getOwnerUserId())) {
            throw exception(CRM_PERMISSION_MODEL_TRANSFER_FAIL_OWNER_USER_NOT_EXISTS, crmName);
        }
        // 1.3 校验新负责人是否存在
        AdminUserRespDTO user = adminUserApi.getUser(permission.getOwnerUserId());
        if (user == null) {
            throw exception(CRM_PERMISSION_MODEL_TRANSFER_FAIL_OWNER_USER_EXISTS, crmName);
        }
        // TODO 校验是否为超级管理员 || 1.4
        // 1.4 校验是否有写权限
        if (!CollUtil.contains(permission.getRwUserIds(), id -> ObjUtil.equal(id, transferCrmPermissionBO.getUserId()))) {
            throw exception(CRM_PERMISSION_DENIED, crmName);
        }

        // 2 权限转移
        CrmPermissionDO updateCrmPermission = new CrmPermissionDO().setId(permission.getId())
                .setOwnerUserId(transferCrmPermissionBO.getOwnerUserId());
        if (ObjUtil.equal(TransferTypeEnum.TEAM.getType(), transferCrmPermissionBO.getTransferType())) {
            if (ObjUtil.equal(PermissionTypeEnum.READONLY.getType(), transferCrmPermissionBO.getPermissionType())) {
                Set<Long> roUserIds = permission.getRoUserIds();
                roUserIds.add(permission.getOwnerUserId()); // 老负责人加入团队有只读权限
                updateCrmPermission.setRoUserIds(roUserIds);
            }
            if (ObjUtil.equal(PermissionTypeEnum.READ_AND_WRITE.getType(), transferCrmPermissionBO.getPermissionType())) {
                Set<Long> rwUserIds = permission.getRwUserIds();
                rwUserIds.add(permission.getOwnerUserId()); // 老负责人加入团队有读写权限
                updateCrmPermission.setRoUserIds(rwUserIds);
            }
        }
        crmPermissionMapper.updateById(updateCrmPermission);

        // 3. TODO 记录机转移日志
    }

}
