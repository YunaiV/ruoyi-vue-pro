package cn.iocoder.yudao.module.srm.api.supplier;

import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.srm.api.supplier.dto.SrmSupplierDTO;
import cn.iocoder.yudao.module.srm.dal.dataobject.purchase.SrmSupplierDO;
import cn.iocoder.yudao.module.srm.service.purchase.SrmSupplierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * 供应商 API 实现类
 *
 * @author wdy
 */
@Service
@Validated
public class SrmSupplierApiImpl implements SrmSupplierApi {

    @Autowired
    @Lazy
    private SrmSupplierService supplierService;

    @Override
    public SrmSupplierDTO getSupplier(Long id) {
        SrmSupplierDO supplier = supplierService.getSupplier(id);
        return BeanUtils.toBean(supplier, SrmSupplierDTO.class);
    }

    @Override
    public List<SrmSupplierDTO> getSupplierList(Collection<Long> ids) {
        List<SrmSupplierDO> list = supplierService.getSupplierList(ids);
        return BeanUtils.toBean(list, SrmSupplierDTO.class);
    }

    @Override
    public Map<Long, SrmSupplierDTO> getSupplierMap(Collection<Long> ids) {
        List<SrmSupplierDTO> list = getSupplierList(ids);
        return convertMap(list, SrmSupplierDTO::getId);
    }
} 