package cn.iocoder.yudao.module.crm.service.permission;

import cn.iocoder.yudao.module.crm.convert.permission.CrmPermissionConvert;
import cn.iocoder.yudao.module.crm.dal.dataobject.permission.CrmPermissionDO;
import cn.iocoder.yudao.module.crm.dal.mysql.permission.CrmPermissionMapper;
import cn.iocoder.yudao.module.crm.service.permission.bo.CrmPermissionCreateBO;
import cn.iocoder.yudao.module.crm.service.permission.bo.CrmPermissionUpdateBO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.crm.enums.ErrorCodeConstants.CRM_PERMISSION_NOT_EXISTS;

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
    public CrmPermissionDO getCrmPermission(Long id) {
        return crmPermissionMapper.selectById(id);
    }

}
