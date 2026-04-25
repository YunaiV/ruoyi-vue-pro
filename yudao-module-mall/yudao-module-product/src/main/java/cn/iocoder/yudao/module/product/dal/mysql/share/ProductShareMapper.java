package cn.iocoder.yudao.module.product.dal.mysql.share;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.product.dal.dataobject.share.ProductShareDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品分享 Mapper
 *
 * @author deepay
 */
@Mapper
public interface ProductShareMapper extends BaseMapperX<ProductShareDO> {

    default ProductShareDO selectByToken(String token) {
        return selectOne(new LambdaQueryWrapperX<ProductShareDO>()
                .eq(ProductShareDO::getToken, token)
                .eq(ProductShareDO::getStatus, 1));
    }

}
