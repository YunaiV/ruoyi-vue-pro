package cn.iocoder.yudao.module.crm.service.businessstatus;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.crm.controller.admin.businessstatus.vo.CrmBusinessStatusCreateReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.businessstatus.vo.CrmBusinessStatusExportReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.businessstatus.vo.CrmBusinessStatusPageReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.businessstatus.vo.CrmBusinessStatusUpdateReqVO;
import cn.iocoder.yudao.module.crm.convert.businessstatus.CrmBusinessStatusConvert;
import cn.iocoder.yudao.module.crm.dal.dataobject.businessstatus.CrmBusinessStatusDO;
import cn.iocoder.yudao.module.crm.dal.mysql.businessstatus.CrmBusinessStatusMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.crm.enums.ErrorCodeConstants.BUSINESS_STATUS_NOT_EXISTS;

/**
 * 商机状态 Service 实现类
 *
 * @author ljlleo
 */
@Service
@Validated
public class CrmBusinessStatusServiceImpl implements CrmBusinessStatusService {

    @Resource
    private CrmBusinessStatusMapper businessStatusMapper;

    @Override
    public Long createBusinessStatus(CrmBusinessStatusCreateReqVO createReqVO) {
        // 插入
        CrmBusinessStatusDO businessStatus = CrmBusinessStatusConvert.INSTANCE.convert(createReqVO);
        businessStatusMapper.insert(businessStatus);
        // 返回
        return businessStatus.getId();
    }

    @Override
    public void updateBusinessStatus(CrmBusinessStatusUpdateReqVO updateReqVO) {
        // 校验存在
        validateBusinessStatusExists(updateReqVO.getId());
        // 更新
        CrmBusinessStatusDO updateObj = CrmBusinessStatusConvert.INSTANCE.convert(updateReqVO);
        businessStatusMapper.updateById(updateObj);
    }

    @Override
    public void deleteBusinessStatus(Long id) {
        // 校验存在
        validateBusinessStatusExists(id);
        // 删除
        businessStatusMapper.deleteById(id);
    }

    private void validateBusinessStatusExists(Long id) {
        if (businessStatusMapper.selectById(id) == null) {
            throw exception(BUSINESS_STATUS_NOT_EXISTS);
        }
    }

    @Override
    public CrmBusinessStatusDO getBusinessStatus(Long id) {
        return businessStatusMapper.selectById(id);
    }

    @Override
    public List<CrmBusinessStatusDO> getBusinessStatusList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return ListUtil.empty();
        }
        return businessStatusMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<CrmBusinessStatusDO> getBusinessStatusPage(CrmBusinessStatusPageReqVO pageReqVO) {
        return businessStatusMapper.selectPage(pageReqVO);
    }

    @Override
    public List<CrmBusinessStatusDO> getBusinessStatusList(CrmBusinessStatusExportReqVO exportReqVO) {
        return businessStatusMapper.selectList(exportReqVO);
    }

    @Override
    public List<CrmBusinessStatusDO> getBusinessStatusListByTypeId(Integer typeId) {
        return businessStatusMapper.getBusinessStatusListByTypeId(typeId);
    }

    @Override
    public List<CrmBusinessStatusDO> getBusinessStatusList() {
        return businessStatusMapper.selectList();
    }
}
