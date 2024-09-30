package cn.iocoder.yudao.module.iot.dal.mysql.thinkmodelfunction;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.thinkmodelfunction.vo.IotThinkModelFunctionPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.thinkmodelfunction.IotThinkModelFunctionDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * IoT 产品物模型 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface IotThinkModelFunctionMapper extends BaseMapperX<IotThinkModelFunctionDO> {

    default PageResult<IotThinkModelFunctionDO> selectPage(IotThinkModelFunctionPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<IotThinkModelFunctionDO>()
                .eqIfPresent(IotThinkModelFunctionDO::getIdentifier, reqVO.getIdentifier())
                .likeIfPresent(IotThinkModelFunctionDO::getName, reqVO.getName())
                .eqIfPresent(IotThinkModelFunctionDO::getType, reqVO.getType())
                .eqIfPresent(IotThinkModelFunctionDO::getProductId, reqVO.getProductId())
                .notIn(IotThinkModelFunctionDO::getIdentifier, "get", "set", "post")
                .orderByDesc(IotThinkModelFunctionDO::getId));
    }


    default IotThinkModelFunctionDO selectByProductIdAndIdentifier(Long productId, String identifier) {
        return selectOne(IotThinkModelFunctionDO::getProductId, productId,
                IotThinkModelFunctionDO::getIdentifier, identifier);
    }

    default List<IotThinkModelFunctionDO> selectListByProductId(Long productId) {
        return selectList(IotThinkModelFunctionDO::getProductId, productId);
    }

    default List<IotThinkModelFunctionDO> selectListByProductIdAndType(Long productId, Integer type) {
        return selectList(IotThinkModelFunctionDO::getProductId, productId,
                IotThinkModelFunctionDO::getType, type);
    }

    default List<IotThinkModelFunctionDO> selectListByProductIdAndIdentifiersAndTypes(Long productId,
                                                                                      List<String> identifiers,
                                                                                      List<Integer> types){
        return selectList(new LambdaQueryWrapperX<IotThinkModelFunctionDO>()
                .eq(IotThinkModelFunctionDO::getProductId, productId)
                .in(IotThinkModelFunctionDO::getIdentifier, identifiers)
                .in(IotThinkModelFunctionDO::getType, types));
    }

    default IotThinkModelFunctionDO selectByProductIdAndName(Long productId, String name) {
        return selectOne(IotThinkModelFunctionDO::getProductId, productId,
                IotThinkModelFunctionDO::getName, name);
    }
}