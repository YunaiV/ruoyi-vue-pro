package cn.iocoder.yudao.module.crm.service.business;

import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.module.crm.dal.dataobject.business.CrmBusinessProductDO;
import cn.iocoder.yudao.module.crm.dal.mysql.business.CrmBusinessProductMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * 商机产品关联表 Service 实现类
 *
 * @author lzxhqs
 */
@Service
@Validated
public class CrmBusinessProductServiceImpl implements CrmBusinessProductService {

    @Resource
    private CrmBusinessProductMapper businessProductMapper;

    @Override
    public void createBusinessProductBatch(List<CrmBusinessProductDO> list) {
        businessProductMapper.insertBatch(list);
    }

    @Override
    public List<CrmBusinessProductDO> getBusinessProductListByBusinessId(Long businessId) {
        return businessProductMapper.selectList(CrmBusinessProductDO::getBusinessId, businessId);
    }

    @Override
    public void updateBusinessProductBatch(List<CrmBusinessProductDO> list) {
        businessProductMapper.updateBatch(list);
    }

    // TODO @lzxhqs：这个方法，可以直接调用 deleteList 方法，然后传递 ids 就好了；
    @Override
    public void deleteBusinessProductBatch(List<CrmBusinessProductDO> list) {
        businessProductMapper.deleteBatchIds(CollectionUtils.convertList(list, CrmBusinessProductDO::getId));
    }

    @Override
    public void deleteBusinessProductByBusinessId(Long businessId) {
        businessProductMapper.deleteByBusinessId(businessId);
    }

    @Override
    public List<CrmBusinessProductDO> getBusinessProductListByContractId(Long contractId) {
        return businessProductMapper.selectList(CrmBusinessProductDO::getContractId, contractId);
    }

}
