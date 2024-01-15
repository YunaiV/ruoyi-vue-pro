package cn.iocoder.yudao.module.crm.convert.businessproduct;

import cn.iocoder.yudao.module.crm.controller.admin.business.vo.product.CrmBusinessProductSaveReqVO;
import cn.iocoder.yudao.module.crm.dal.dataobject.business.CrmBusinessProductDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * @author lzxhqs
 * @version 1.0
 * @title CrmBusinessProductConvert
 * @description
 * @create 2024/1/12
 */
@Mapper
public interface CrmBusinessProductConvert {
    CrmBusinessProductConvert INSTANCE = Mappers.getMapper(CrmBusinessProductConvert.class);

    CrmBusinessProductDO convert(CrmBusinessProductSaveReqVO product);
}
