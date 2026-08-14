package cn.iocoder.yudao.module.fms.dal.mysql.config;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.fms.controller.admin.config.vo.auxiliaryitem.FmsAuxiliaryItemPageReqVO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsAuxiliaryItemDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

/**
 * FMS 辅助核算项目 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface FmsAuxiliaryItemMapper extends BaseMapperX<FmsAuxiliaryItemDO> {

    default PageResult<FmsAuxiliaryItemDO> selectPage(FmsAuxiliaryItemPageReqVO reqVO) {
        LambdaQueryWrapperX<FmsAuxiliaryItemDO> query = new LambdaQueryWrapperX<FmsAuxiliaryItemDO>()
                .eq(FmsAuxiliaryItemDO::getAccountSetId, reqVO.getAccountSetId())
                .eq(FmsAuxiliaryItemDO::getAuxiliaryTypeId, reqVO.getAuxiliaryTypeId());
        if (StrUtil.isNotBlank(reqVO.getSearch())) {
            query.and(wrapper -> wrapper.like(FmsAuxiliaryItemDO::getCode, reqVO.getSearch())
                    .or().like(FmsAuxiliaryItemDO::getName, reqVO.getSearch()));
        }
        return selectPage(reqVO, query.orderByDesc(FmsAuxiliaryItemDO::getCode)
                .orderByDesc(FmsAuxiliaryItemDO::getId));
    }

    default FmsAuxiliaryItemDO selectByTypeIdAndCode(Long accountSetId, Long auxiliaryTypeId, String code) {
        return selectOne(new LambdaQueryWrapperX<FmsAuxiliaryItemDO>()
                .eq(FmsAuxiliaryItemDO::getAccountSetId, accountSetId)
                .eq(FmsAuxiliaryItemDO::getAuxiliaryTypeId, auxiliaryTypeId)
                .eq(FmsAuxiliaryItemDO::getCode, code));
    }

    default Long selectCountByAuxiliaryTypeId(Long accountSetId, Long auxiliaryTypeId) {
        return selectCount(new LambdaQueryWrapperX<FmsAuxiliaryItemDO>()
                .eq(FmsAuxiliaryItemDO::getAccountSetId, accountSetId)
                .eq(FmsAuxiliaryItemDO::getAuxiliaryTypeId, auxiliaryTypeId));
    }

    default List<FmsAuxiliaryItemDO> selectListByIdsAndAccountSetId(Collection<Long> ids, Long accountSetId) {
        return selectList(new LambdaQueryWrapperX<FmsAuxiliaryItemDO>()
                .in(FmsAuxiliaryItemDO::getId, ids)
                .eq(FmsAuxiliaryItemDO::getAccountSetId, accountSetId));
    }

    default List<FmsAuxiliaryItemDO> selectListByAccountSetIdAndAuxiliaryTypeId(
            Long accountSetId, Long auxiliaryTypeId) {
        return selectList(new LambdaQueryWrapperX<FmsAuxiliaryItemDO>()
                .eq(FmsAuxiliaryItemDO::getAccountSetId, accountSetId)
                .eq(FmsAuxiliaryItemDO::getAuxiliaryTypeId, auxiliaryTypeId)
                .orderByDesc(FmsAuxiliaryItemDO::getCode)
                .orderByDesc(FmsAuxiliaryItemDO::getId));
    }

    default List<FmsAuxiliaryItemDO> selectListByAccountSetIdAndAuxiliaryTypeIdAndStatus(
            Long accountSetId, Long auxiliaryTypeId, Integer status) {
        return selectList(new LambdaQueryWrapperX<FmsAuxiliaryItemDO>()
                .eq(FmsAuxiliaryItemDO::getAccountSetId, accountSetId)
                .eq(FmsAuxiliaryItemDO::getAuxiliaryTypeId, auxiliaryTypeId)
                .eq(FmsAuxiliaryItemDO::getStatus, status)
                .orderByDesc(FmsAuxiliaryItemDO::getCode)
                .orderByDesc(FmsAuxiliaryItemDO::getId));
    }

    default List<FmsAuxiliaryItemDO> selectListByAccountSetId(Long accountSetId) {
        return selectList(new LambdaQueryWrapperX<FmsAuxiliaryItemDO>()
                .eq(FmsAuxiliaryItemDO::getAccountSetId, accountSetId)
                .orderByAsc(FmsAuxiliaryItemDO::getAuxiliaryTypeId)
                .orderByDesc(FmsAuxiliaryItemDO::getCode)
                .orderByDesc(FmsAuxiliaryItemDO::getId));
    }

}
