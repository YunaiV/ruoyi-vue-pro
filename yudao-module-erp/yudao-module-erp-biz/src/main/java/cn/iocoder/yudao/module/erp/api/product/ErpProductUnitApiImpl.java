package cn.iocoder.yudao.module.erp.api.product;

import cn.iocoder.yudao.module.erp.api.product.dto.ErpProductUnitDTO;
import cn.iocoder.yudao.module.erp.convert.product.ErpProductUnitConvert;
import cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpProductUnitDO;
import cn.iocoder.yudao.module.erp.service.product.ErpProductUnitService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ErpProductUnitApiImpl implements ErpProductUnitApi{

    private final ErpProductUnitService erpProductUnitService;
    @Override
    public Map<Long, ErpProductUnitDTO> getProductUnitMap(Collection<Long> ids) {
        Map<Long, ErpProductUnitDO> productUnitMap = erpProductUnitService.getProductUnitMap(ids);
        return ErpProductUnitConvert.INSTANCE.convert(productUnitMap);
    }

    @Override
    public List<ErpProductUnitDTO> getProductUnitList(Collection<Long> ids) {
        List<ErpProductUnitDO> list = erpProductUnitService.getProductUnitList(ids);
        return ErpProductUnitConvert.INSTANCE.convert(list);
    }
}
