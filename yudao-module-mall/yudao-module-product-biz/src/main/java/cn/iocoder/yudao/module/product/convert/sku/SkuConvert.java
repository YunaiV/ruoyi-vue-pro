package cn.iocoder.yudao.module.product.convert.sku;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import cn.iocoder.yudao.module.product.controller.admin.sku.vo.*;
import cn.iocoder.yudao.module.product.dal.dataobject.sku.SkuDO;

/**
 * 商品sku Convert
 *
 * @author 芋道源码
 */
@Mapper
public interface SkuConvert {

    SkuConvert INSTANCE = Mappers.getMapper(SkuConvert.class);

    SkuDO convert(SkuCreateReqVO bean);

    SkuDO convert(SkuUpdateReqVO bean);

    SkuRespVO convert(SkuDO bean);

    List<SkuRespVO> convertList(List<SkuDO> list);

    PageResult<SkuRespVO> convertPage(PageResult<SkuDO> page);

    List<SkuExcelVO> convertList02(List<SkuDO> list);

}
