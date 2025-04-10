package cn.iocoder.yudao.module.erp.api.product;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.exception.util.ThrowUtil;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.erp.api.product.dto.ErpProductDTO;
import cn.iocoder.yudao.module.erp.api.product.dto.ErpProductRespDTO;
import cn.iocoder.yudao.module.erp.convert.product.ErpProductConvert;
import cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpProductDO;
import cn.iocoder.yudao.module.erp.dal.mysql.product.ErpProductMapper;
import cn.iocoder.yudao.module.erp.service.product.ErpProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.PRODUCT_NOT_ENABLE;
import static cn.iocoder.yudao.module.erp.enums.ErrorCodeConstants.PRODUCT_NOT_EXISTS;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ErpProductApiImpl implements ErpProductApi {
    private final ErpProductMapper erpProductMapper;

    private final ErpProductService erpProductService;

    @Override
    public ErpProductDTO getProductDto(Long id) {
        ErpProductDO productDO = erpProductMapper.selectById(id);
        if (productDO == null) {
            return null;
        } else {
            return ErpProductConvert.INSTANCE.convert(productDO);
        }
    }

    //获得所有产品DTO，根据ids，如果ids为null返回所有
    public List<ErpProductDTO> listProductDTOs(List<Long> ids) {
        List<ErpProductDO> dos;
        if (ids != null) {
            dos = erpProductMapper.selectBatchIds(ids);
        } else {
            dos = erpProductMapper.selectList();
        }
        return ErpProductConvert.INSTANCE.convert(dos);
    }

    @Override
    public Map<Long, ErpProductDTO> getProductMap(Collection<Long> ids) {
        Map<Long, ErpProductDO> productMap = convertMap(erpProductMapper.selectBatchIds(ids), ErpProductDO::getId);
        return ErpProductConvert.INSTANCE.convert(productMap);

    }

    @Override
    public List<ErpProductDTO> listProducts(Collection<Long> ids) {
        List<ErpProductDO> erpProductDOs = erpProductMapper.selectBatchIds(ids);
        return ErpProductConvert.INSTANCE.convert(erpProductDOs);
    }

    @Override
    public List<ErpProductDTO> validProductList(Collection<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        List<ErpProductDTO> list = BeanUtils.toBean(erpProductMapper.selectBatchIds(ids), ErpProductDTO.class);
        Map<Long, ErpProductDTO> productMap = convertMap(list, ErpProductDTO::getId);
        for (Long id : ids) {
            ErpProductDTO product = productMap.get(id);
            ThrowUtil.ifEmptyThrow(product, PRODUCT_NOT_EXISTS);
            //校验产品是否是启用状态
            ThrowUtil.ifThrow(!product.getStatus(), PRODUCT_NOT_ENABLE, product.getName());
        }
        return list;
    }

    @Override
    public List<ErpProductRespDTO> getProductDTOListByStatus(Boolean status) {
        return erpProductService.getProductDTOListByStatus(status);
    }

    @Override
    public List<Long> listProductIdByBarCode(String barCode) {
        return erpProductService.listProductIdByBarCode(barCode);
    }
}
