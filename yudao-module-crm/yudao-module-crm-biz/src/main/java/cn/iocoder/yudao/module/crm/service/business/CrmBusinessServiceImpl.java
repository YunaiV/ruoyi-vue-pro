package cn.iocoder.yudao.module.crm.service.business;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.crm.controller.admin.business.vo.CrmBusinessCreateReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.business.vo.CrmBusinessExportReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.business.vo.CrmBusinessPageReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.business.vo.CrmBusinessUpdateReqVO;
import cn.iocoder.yudao.module.crm.convert.business.CrmBusinessConvert;
import cn.iocoder.yudao.module.crm.dal.dataobject.business.CrmBusinessDO;
import cn.iocoder.yudao.module.crm.dal.mysql.business.CrmBusinessMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.crm.enums.ErrorCodeConstants.BUSINESS_NOT_EXISTS;

/**
 * 商机 Service 实现类
 *
 * @author ljlleo
 */
@Service
@Validated
public class CrmBusinessServiceImpl implements CrmBusinessService {

    @Resource
    private CrmBusinessMapper businessMapper;

    @Override
    public Long createBusiness(CrmBusinessCreateReqVO createReqVO) {
        // 插入
        CrmBusinessDO business = CrmBusinessConvert.INSTANCE.convert(createReqVO);
        businessMapper.insert(business);
        // 返回
        return business.getId();
    }

    @Override
    public void updateBusiness(CrmBusinessUpdateReqVO updateReqVO) {
        // 校验存在
        validateBusinessExists(updateReqVO.getId());
        // 更新
        CrmBusinessDO updateObj = CrmBusinessConvert.INSTANCE.convert(updateReqVO);
        businessMapper.updateById(updateObj);
    }

    @Override
    public void deleteBusiness(Long id) {
        // 校验存在
        validateBusinessExists(id);
        // 删除
        businessMapper.deleteById(id);
    }

    private void validateBusinessExists(Long id) {
        if (businessMapper.selectById(id) == null) {
            throw exception(BUSINESS_NOT_EXISTS);
        }
    }

    @Override
    public CrmBusinessDO getBusiness(Long id) {
        return businessMapper.selectById(id);
    }

    @Override
    public List<CrmBusinessDO> getBusinessList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return ListUtil.empty();
        }
        return businessMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<CrmBusinessDO> getBusinessPage(CrmBusinessPageReqVO pageReqVO) {
        return businessMapper.selectPage(pageReqVO);
    }

    @Override
    public List<CrmBusinessDO> getBusinessList(CrmBusinessExportReqVO exportReqVO) {
        return businessMapper.selectList(exportReqVO);
    }

}
