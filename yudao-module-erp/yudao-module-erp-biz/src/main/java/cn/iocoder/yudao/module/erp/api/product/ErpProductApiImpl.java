package cn.iocoder.yudao.module.erp.api.product;

import cn.iocoder.yudao.module.erp.api.product.dto.ErpProductDTO;
import cn.iocoder.yudao.module.erp.convert.product.ErpProductConvert;
import cn.iocoder.yudao.module.erp.dal.dataobject.product.ErpProductDO;
import cn.iocoder.yudao.module.erp.dal.mysql.product.ErpProductMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ErpProductApiImpl implements ErpProductApi {
    private final ErpProductMapper erpProductMapper;

    //获得所有产品DTO，根据ids，如果ids为null返回所有
    public List<ErpProductDTO> listProductDTOs(List<Long> ids) {
        List<ErpProductDO> dos;
        if (ids != null) {
            dos = erpProductMapper.selectBatchIds(ids);
        }else {
            dos = erpProductMapper.selectList();
        }
        return ErpProductConvert.INSTANCE.convert(dos);
    }
}
