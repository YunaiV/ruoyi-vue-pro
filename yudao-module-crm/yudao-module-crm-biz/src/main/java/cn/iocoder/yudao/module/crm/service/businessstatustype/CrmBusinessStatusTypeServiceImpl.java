package cn.iocoder.yudao.module.crm.service.businessstatustype;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.crm.controller.admin.businessstatustype.vo.CrmBusinessStatusTypeCreateReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.businessstatustype.vo.CrmBusinessStatusTypeExportReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.businessstatustype.vo.CrmBusinessStatusTypePageReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.businessstatustype.vo.CrmBusinessStatusTypeUpdateReqVO;
import cn.iocoder.yudao.module.crm.convert.businessstatustype.CrmBusinessStatusTypeConvert;
import cn.iocoder.yudao.module.crm.dal.dataobject.businessstatustype.CrmBusinessStatusTypeDO;
import cn.iocoder.yudao.module.crm.dal.mysql.businessstatustype.CrmBusinessStatusTypeMapper;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.crm.enums.ErrorCodeConstants.BUSINESS_STATUS_TYPE_NOT_EXISTS;

/**
 * 商机状态类型 Service 实现类
 *
 * @author ljlleo
 */
@Service
@Validated
public class CrmBusinessStatusTypeServiceImpl implements CrmBusinessStatusTypeService {

    @Resource
    private CrmBusinessStatusTypeMapper businessStatusTypeMapper;

    @Override
    public Long createBusinessStatusType(CrmBusinessStatusTypeCreateReqVO createReqVO) {
        // TODO ljlleo：name 应该需要唯一哈；
        // 插入
        CrmBusinessStatusTypeDO businessStatusType = CrmBusinessStatusTypeConvert.INSTANCE.convert(createReqVO);
        businessStatusTypeMapper.insert(businessStatusType);
        // 返回
        return businessStatusType.getId();
    }

    @Override
    public void updateBusinessStatusType(CrmBusinessStatusTypeUpdateReqVO updateReqVO) {
        // TODO ljlleo：name 应该需要唯一哈；
        // 校验存在
        validateBusinessStatusTypeExists(updateReqVO.getId());
        // 更新
        CrmBusinessStatusTypeDO updateObj = CrmBusinessStatusTypeConvert.INSTANCE.convert(updateReqVO);
        businessStatusTypeMapper.updateById(updateObj);
    }

    @Override
    public void deleteBusinessStatusType(Long id) {
        // 校验存在
        validateBusinessStatusTypeExists(id);
        // TODO 艿艿：这里在看看，是不是要校验业务是否在使用；
        // 删除
        businessStatusTypeMapper.deleteById(id);
    }

    private void validateBusinessStatusTypeExists(Long id) {
        if (businessStatusTypeMapper.selectById(id) == null) {
            throw exception(BUSINESS_STATUS_TYPE_NOT_EXISTS);
        }
    }

    @Override
    public CrmBusinessStatusTypeDO getBusinessStatusType(Long id) {
        return businessStatusTypeMapper.selectById(id);
    }

    @Override
    public List<CrmBusinessStatusTypeDO> getBusinessStatusTypeList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return ListUtil.empty();
        }
        return businessStatusTypeMapper.selectBatchIds(ids);
    }

    @Override
    public PageResult<CrmBusinessStatusTypeDO> getBusinessStatusTypePage(CrmBusinessStatusTypePageReqVO pageReqVO) {
        return businessStatusTypeMapper.selectPage(pageReqVO);
    }

    @Override
    public List<CrmBusinessStatusTypeDO> getBusinessStatusTypeList(CrmBusinessStatusTypeExportReqVO exportReqVO) {
        return businessStatusTypeMapper.selectList(exportReqVO);
    }

    @Override
    public List<CrmBusinessStatusTypeDO> getBusinessStatusTypeListByStatus(Integer status) {
        return businessStatusTypeMapper.getBusinessStatusTypeListByStatus(status);
    }

}
