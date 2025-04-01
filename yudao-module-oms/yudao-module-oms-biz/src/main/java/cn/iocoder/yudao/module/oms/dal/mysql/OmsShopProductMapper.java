package cn.iocoder.yudao.module.oms.dal.mysql;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.MPJLambdaWrapperX;
import cn.iocoder.yudao.module.oms.dal.dataobject.OmsShopDO;
import cn.iocoder.yudao.module.oms.dal.dataobject.OmsShopProductDO;
import com.github.yulichang.wrapper.MPJLambdaWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;


/**
 * OMS店铺产品 Mapper
 *
 * @author gumaomao
 */
@Mapper
public interface OmsShopProductMapper extends BaseMapperX<OmsShopProductDO> {
    default List<OmsShopProductDO> getByPlatformCode(String platformCode) {
        MPJLambdaWrapper<OmsShopProductDO> wrapper = new MPJLambdaWrapperX<OmsShopProductDO>()
            .selectAll(OmsShopProductDO.class)  // 选择所有列
            .leftJoin(OmsShopDO.class, OmsShopDO::getId, OmsShopProductDO::getShopId)
            .eq(OmsShopDO::getPlatformCode, platformCode);

        return selectJoinList(OmsShopProductDO.class, wrapper);
    }
}