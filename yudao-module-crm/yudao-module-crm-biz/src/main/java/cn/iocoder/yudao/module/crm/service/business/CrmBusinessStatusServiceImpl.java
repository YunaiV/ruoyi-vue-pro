package cn.iocoder.yudao.module.crm.service.business;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.crm.controller.admin.business.vo.status.CrmBusinessStatusPageReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.business.vo.status.CrmBusinessStatusQueryVO;
import cn.iocoder.yudao.module.crm.controller.admin.business.vo.status.CrmBusinessStatusSaveReqVO;
import cn.iocoder.yudao.module.crm.dal.dataobject.business.CrmBusinessStatusDO;
import cn.iocoder.yudao.module.crm.dal.mysql.business.CrmBusinessStatusMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;

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
    public Long createBusinessStatus(CrmBusinessStatusSaveReqVO createReqVO) {
        // 插入
        CrmBusinessStatusDO businessStatus = BeanUtils.toBean(createReqVO, CrmBusinessStatusDO.class);
        businessStatusMapper.insert(businessStatus);
        // 返回
        return businessStatus.getId();
    }

    @Override
    public void updateBusinessStatus(CrmBusinessStatusSaveReqVO updateReqVO) {
        // 校验存在
        validateBusinessStatusExists(updateReqVO.getId());
        // 更新
        CrmBusinessStatusDO updateObj = BeanUtils.toBean(updateReqVO, CrmBusinessStatusDO.class);
        businessStatusMapper.updateById(updateObj);
    }

    @Override
    public void deleteBusinessStatus(Long id) {
        // 校验存在
        validateBusinessStatusExists(id);
        // TODO @ljlleo 这里可以考虑，如果有商机在使用，不允许删除
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
    public PageResult<CrmBusinessStatusDO> getBusinessStatusPage(CrmBusinessStatusPageReqVO pageReqVO) {
        return businessStatusMapper.selectPage(pageReqVO);
    }

    @Override
    public List<CrmBusinessStatusDO> selectList(CrmBusinessStatusQueryVO queryVO) {
        return businessStatusMapper.selectList(queryVO);
    }

}
