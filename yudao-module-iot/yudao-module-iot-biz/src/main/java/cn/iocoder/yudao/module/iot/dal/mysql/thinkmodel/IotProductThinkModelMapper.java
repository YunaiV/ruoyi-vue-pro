package cn.iocoder.yudao.module.iot.dal.mysql.thinkmodel;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.thinkmodel.vo.IotProductThinkModelPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.thinkmodel.IotProductThinkModelDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * IoT 产品物模型 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface IotProductThinkModelMapper extends BaseMapperX<IotProductThinkModelDO> {

    default PageResult<IotProductThinkModelDO> selectPage(IotProductThinkModelPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<IotProductThinkModelDO>()
                .eqIfPresent(IotProductThinkModelDO::getIdentifier, reqVO.getIdentifier())
                .likeIfPresent(IotProductThinkModelDO::getName, reqVO.getName())
                .eqIfPresent(IotProductThinkModelDO::getType, reqVO.getType())
                .eqIfPresent(IotProductThinkModelDO::getProductId, reqVO.getProductId())
                .notIn(IotProductThinkModelDO::getIdentifier, "get", "set", "post")
                .orderByDesc(IotProductThinkModelDO::getId));
    }

    default IotProductThinkModelDO selectByProductIdAndIdentifier(Long productId, String identifier) {
        return selectOne(IotProductThinkModelDO::getProductId, productId,
                IotProductThinkModelDO::getIdentifier, identifier);
    }

    default List<IotProductThinkModelDO> selectListByProductId(Long productId) {
        return selectList(IotProductThinkModelDO::getProductId, productId);
    }

    default List<IotProductThinkModelDO> selectListByProductIdAndType(Long productId, Integer type) {
        return selectList(IotProductThinkModelDO::getProductId, productId,
                IotProductThinkModelDO::getType, type);
    }

    default List<IotProductThinkModelDO> selectListByProductIdAndIdentifiersAndTypes(Long productId,
                                                                                     List<String> identifiers,
                                                                                     List<Integer> types) {
        return selectList(new LambdaQueryWrapperX<IotProductThinkModelDO>()
                .eq(IotProductThinkModelDO::getProductId, productId)
                .in(IotProductThinkModelDO::getIdentifier, identifiers)
                .in(IotProductThinkModelDO::getType, types));
    }

    default IotProductThinkModelDO selectByProductIdAndName(Long productId, String name) {
        return selectOne(IotProductThinkModelDO::getProductId, productId,
                IotProductThinkModelDO::getName, name);
    }

    default List<IotProductThinkModelDO> selectListByProductKey(String productKey) {
        return selectList(IotProductThinkModelDO::getProductKey, productKey);
    }

}