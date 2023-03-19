package cn.iocoder.yudao.module.product.dal.mysql.comment;


import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.product.dal.dataobject.comment.ProductCommentDO;
import org.apache.ibatis.annotations.Mapper;


/**
 * 商品评论 Mapper
 *
 * @author wangzhs
 */
@Mapper
public interface ProductCommentMapper extends BaseMapperX<ProductCommentDO> {
}
