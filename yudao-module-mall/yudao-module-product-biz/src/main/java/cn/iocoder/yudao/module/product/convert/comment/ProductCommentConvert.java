package cn.iocoder.yudao.module.product.convert.comment;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.product.controller.admin.comment.vo.ProductCommentRespVO;
import cn.iocoder.yudao.module.product.dal.dataobject.comment.ProductCommentDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 商品评价 Convert
 *
 * @author wangzhs
 */
@Mapper
public interface ProductCommentConvert {

    ProductCommentConvert INSTANCE = Mappers.getMapper(ProductCommentConvert.class);

    ProductCommentRespVO convert(ProductCommentDO bean);

    List<ProductCommentRespVO> convertList(List<ProductCommentDO> list);

    PageResult<ProductCommentRespVO> convertPage(PageResult<ProductCommentDO> page);

}
