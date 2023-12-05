package cn.iocoder.yudao.module.crm.convert.product;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.crm.controller.admin.product.vo.product.CrmProductCreateReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.product.vo.product.CrmProductExcelVO;
import cn.iocoder.yudao.module.crm.controller.admin.product.vo.product.CrmProductRespVO;
import cn.iocoder.yudao.module.crm.controller.admin.product.vo.product.CrmProductUpdateReqVO;
import cn.iocoder.yudao.module.crm.dal.dataobject.product.CrmProductDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 产品 Convert
 *
 * @author ZanGe丶
 */
@Mapper
public interface CrmProductConvert {

    CrmProductConvert INSTANCE = Mappers.getMapper(CrmProductConvert.class);

    CrmProductDO convert(CrmProductCreateReqVO bean);

    CrmProductDO convert(CrmProductUpdateReqVO bean);

    CrmProductRespVO convert(CrmProductDO bean);

    List<CrmProductRespVO> convertList(List<CrmProductDO> list);

    PageResult<CrmProductRespVO> convertPage(PageResult<CrmProductDO> page);

    List<CrmProductExcelVO> convertList02(List<CrmProductDO> list);

}
