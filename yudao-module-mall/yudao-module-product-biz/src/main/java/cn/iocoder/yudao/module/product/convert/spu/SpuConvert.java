package cn.iocoder.yudao.module.product.convert.spu;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import cn.iocoder.yudao.module.product.controller.admin.spu.vo.*;
import cn.iocoder.yudao.module.product.dal.dataobject.spu.SpuDO;

/**
 * 商品spu Convert
 *
 * @author 芋道源码
 */
@Mapper
public interface SpuConvert {

    SpuConvert INSTANCE = Mappers.getMapper(SpuConvert.class);

    SpuDO convert(SpuCreateReqVO bean);

    SpuDO convert(SpuUpdateReqVO bean);

    SpuRespVO convert(SpuDO bean);

    List<SpuRespVO> convertList(List<SpuDO> list);

    PageResult<SpuRespVO> convertPage(PageResult<SpuDO> page);

    List<SpuExcelVO> convertList02(List<SpuDO> list);

}
