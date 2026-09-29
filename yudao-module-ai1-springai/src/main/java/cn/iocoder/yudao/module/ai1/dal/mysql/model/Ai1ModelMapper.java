package cn.iocoder.yudao.module.ai1.dal.mysql.model;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.ai1.controller.admin.model.vo.model.Ai1ModelPageReqVO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.model.Ai1ModelDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

/**
 * AI1 模型 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface Ai1ModelMapper extends BaseMapperX<Ai1ModelDO> {

    default PageResult<Ai1ModelDO> selectPage(Ai1ModelPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<Ai1ModelDO>()
                .eqIfPresent(Ai1ModelDO::getProviderId, reqVO.getProviderId())
                .likeIfPresent(Ai1ModelDO::getName, reqVO.getName())
                .eqIfPresent(Ai1ModelDO::getType, reqVO.getType())
                .orderByAsc(Ai1ModelDO::getId));
    }

    default Ai1ModelDO selectByProviderIdAndModel(Long providerId, String model) {
        return selectOne(Ai1ModelDO::getProviderId, providerId, Ai1ModelDO::getModel, model);
    }

    default List<Ai1ModelDO> selectListByProviderId(Long providerId) {
        return selectList(new LambdaQueryWrapperX<Ai1ModelDO>()
                .eq(Ai1ModelDO::getProviderId, providerId)
                .orderByAsc(Ai1ModelDO::getId));
    }

    default List<Ai1ModelDO> selectListByProviderIdAndTypeAndStatus(Long providerId, Integer type, Integer status) {
        return selectList(new LambdaQueryWrapperX<Ai1ModelDO>()
                .eqIfPresent(Ai1ModelDO::getProviderId, providerId)
                .eqIfPresent(Ai1ModelDO::getType, type)
                .eq(Ai1ModelDO::getStatus, status)
                .orderByAsc(Ai1ModelDO::getId));
    }

    default Long selectCountByProviderIds(Collection<Long> providerIds) {
        return selectCount(new LambdaQueryWrapperX<Ai1ModelDO>().in(Ai1ModelDO::getProviderId, providerIds));
    }

}
