package cn.iocoder.yudao.module.iot.dal.mysql.thinkmodelfunction;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.dal.dataobject.thinkmodelfunction.IotThinkModelFunctionDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * IoT 产品物模型 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface IotThinkModelFunctionMapper extends BaseMapperX<IotThinkModelFunctionDO> {

    default IotThinkModelFunctionDO selectByProductKey(String productKey) {
        return selectOne(new LambdaQueryWrapperX<IotThinkModelFunctionDO>().eq(IotThinkModelFunctionDO::getProductKey, productKey));
    }

    default IotThinkModelFunctionDO selectByProductId(Long productId){
        return selectOne(new LambdaQueryWrapperX<IotThinkModelFunctionDO>().eq(IotThinkModelFunctionDO::getProductId, productId));
    }

}