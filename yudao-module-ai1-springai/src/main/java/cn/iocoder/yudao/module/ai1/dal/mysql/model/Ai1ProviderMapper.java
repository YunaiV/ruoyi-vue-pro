package cn.iocoder.yudao.module.ai1.dal.mysql.model;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.ai1.controller.admin.model.vo.provider.Ai1ProviderPageReqVO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.model.Ai1ProviderDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * AI1 供应商 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface Ai1ProviderMapper extends BaseMapperX<Ai1ProviderDO> {

    default PageResult<Ai1ProviderDO> selectPage(Ai1ProviderPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<Ai1ProviderDO>()
                .likeIfPresent(Ai1ProviderDO::getName, reqVO.getName())
                .eqIfPresent(Ai1ProviderDO::getStatus, reqVO.getStatus())
                .orderByAsc(Ai1ProviderDO::getId));
    }

    default List<Ai1ProviderDO> selectListByStatus(Integer status) {
        return selectList(new LambdaQueryWrapperX<Ai1ProviderDO>()
                .eq(Ai1ProviderDO::getStatus, status)
                .orderByAsc(Ai1ProviderDO::getId));
    }

}
