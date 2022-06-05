package cn.iocoder.yudao.module.product.convert.spu;

import java.util.*;

import cn.iocoder.yudao.framework.common.pojo.PageResult;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;
import cn.iocoder.yudao.module.product.controller.admin.spu.vo.*;
import cn.iocoder.yudao.module.product.dal.dataobject.spu.ProductSpuDO;
import org.springframework.util.StringUtils;

/**
 * 商品spu Convert
 *
 * @author 芋道源码
 */
@Mapper
public interface ProductSpuConvert {

    ProductSpuConvert INSTANCE = Mappers.getMapper(ProductSpuConvert.class);

    @Mapping(source = "picUrls", target = "picUrls", qualifiedByName = "translatePicUrlsFromStringList")
    ProductSpuDO convert(ProductSpuCreateReqVO bean);

    @Mapping(source = "picUrls", target = "picUrls", qualifiedByName = "translatePicUrlsFromStringList")
    ProductSpuDO convert(SpuUpdateReqVO bean);

    @Mapping(source = "picUrls", target = "picUrls", qualifiedByName = "tokenizeToStringArray")
    SpuRespVO convert(ProductSpuDO bean);

    @Mapping(source = "picUrls", target = "picUrls", qualifiedByName = "tokenizeToStringArray")
    SpuExcelVO convertToExcelVO(ProductSpuDO bean);

    List<SpuRespVO> convertList(List<ProductSpuDO> list);

    PageResult<SpuRespVO> convertPage(PageResult<ProductSpuDO> page);

    List<SpuExcelVO> convertList02(List<ProductSpuDO> list);

    @Named("tokenizeToStringArray")
    default List<String> translatePicUrlsArrayFromString(String picUrls) {
        return Arrays.asList(StringUtils.tokenizeToStringArray(picUrls, ","));
    }

    @Named("translatePicUrlsFromStringList")
    default String translatePicUrlsFromList(List<String> picUrls) {
        return StringUtils.collectionToCommaDelimitedString(picUrls);
    }
}
