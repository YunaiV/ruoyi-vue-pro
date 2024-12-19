package cn.iocoder.yudao.module.iot.dal.mysql.thingmodel;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.thingmodel.vo.IotProductThingModelPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.thingmodel.IotProductThingModelDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * IoT 产品物模型 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface IotProductThingModelMapper extends BaseMapperX<IotProductThingModelDO> {

    default PageResult<IotProductThingModelDO> selectPage(IotProductThingModelPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<IotProductThingModelDO>()
                .eqIfPresent(IotProductThingModelDO::getIdentifier, reqVO.getIdentifier())
                .likeIfPresent(IotProductThingModelDO::getName, reqVO.getName())
                .eqIfPresent(IotProductThingModelDO::getType, reqVO.getType())
                .eqIfPresent(IotProductThingModelDO::getProductId, reqVO.getProductId())
                .notIn(IotProductThingModelDO::getIdentifier, "get", "set", "post")
                .orderByDesc(IotProductThingModelDO::getId));
    }

    default IotProductThingModelDO selectByProductIdAndIdentifier(Long productId, String identifier) {
        return selectOne(IotProductThingModelDO::getProductId, productId,
                IotProductThingModelDO::getIdentifier, identifier);
    }

    default List<IotProductThingModelDO> selectListByProductId(Long productId) {
        return selectList(IotProductThingModelDO::getProductId, productId);
    }

    default List<IotProductThingModelDO> selectListByProductIdAndType(Long productId, Integer type) {
        return selectList(IotProductThingModelDO::getProductId, productId,
                IotProductThingModelDO::getType, type);
    }

    default List<IotProductThingModelDO> selectListByProductIdAndIdentifiersAndTypes(Long productId,
                                                                                     List<String> identifiers,
                                                                                     List<Integer> types) {
        return selectList(new LambdaQueryWrapperX<IotProductThingModelDO>()
                .eq(IotProductThingModelDO::getProductId, productId)
                .in(IotProductThingModelDO::getIdentifier, identifiers)
                .in(IotProductThingModelDO::getType, types));
    }

    default IotProductThingModelDO selectByProductIdAndName(Long productId, String name) {
        return selectOne(IotProductThingModelDO::getProductId, productId,
                IotProductThingModelDO::getName, name);
    }

    default List<IotProductThingModelDO> selectListByProductKey(String productKey) {
        return selectList(IotProductThingModelDO::getProductKey, productKey);
    }

}