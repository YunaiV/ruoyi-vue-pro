package cn.iocoder.yudao.module.iot.dal.mysql.thingmodel;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.thingmodel.vo.IotThingModelListReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.thingmodel.vo.IotThingModelPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.thingmodel.IotThingModelDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

/**
 * IoT 产品物模型 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface IotThingModelMapper extends BaseMapperX<IotThingModelDO> {

    default PageResult<IotThingModelDO> selectPage(IotThingModelPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<IotThingModelDO>()
                .eqIfPresent(IotThingModelDO::getIdentifier, reqVO.getIdentifier())
                .likeIfPresent(IotThingModelDO::getName, reqVO.getName())
                .eqIfPresent(IotThingModelDO::getType, reqVO.getType())
                .eqIfPresent(IotThingModelDO::getProductId, reqVO.getProductId())
                .orderByDesc(IotThingModelDO::getId));
    }

    default List<IotThingModelDO> selectList(IotThingModelListReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<IotThingModelDO>()
                .eqIfPresent(IotThingModelDO::getIdentifier, reqVO.getIdentifier())
                .likeIfPresent(IotThingModelDO::getName, reqVO.getName())
                .eqIfPresent(IotThingModelDO::getType, reqVO.getType())
                .eqIfPresent(IotThingModelDO::getProductId, reqVO.getProductId())
                .orderByDesc(IotThingModelDO::getId));
    }

    default IotThingModelDO selectByProductIdAndIdentifier(Long productId, String identifier) {
        return selectOne(IotThingModelDO::getProductId, productId,
                IotThingModelDO::getIdentifier, identifier);
    }

    default List<IotThingModelDO> selectListByProductId(Long productId) {
        return selectList(IotThingModelDO::getProductId, productId);
    }

    default List<IotThingModelDO> selectListByProductIdAndIdentifiers(Long productId, Collection<String> identifiers) {
        return selectList(new LambdaQueryWrapperX<IotThingModelDO>()
                .eq(IotThingModelDO::getProductId, productId)
                .in(IotThingModelDO::getIdentifier, identifiers));
    }

    default List<IotThingModelDO> selectListByProductIdAndType(Long productId, Integer type) {
        return selectList(IotThingModelDO::getProductId, productId,
                IotThingModelDO::getType, type);
    }

    default IotThingModelDO selectByProductIdAndName(Long productId, String name) {
        return selectOne(IotThingModelDO::getProductId, productId,
                IotThingModelDO::getName, name);
    }

}