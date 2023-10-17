package cn.iocoder.yudao.module.crm.dal.mysql.contract;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.crm.controller.admin.contract.vo.ContractExportReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.contract.vo.ContractPageReqVO;
import cn.iocoder.yudao.module.crm.dal.dataobject.contract.ContractDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 合同 Mapper
 *
 * @author dhb52
 */
@Mapper
public interface ContractMapper extends BaseMapperX<ContractDO> {

    default PageResult<ContractDO> selectPage(ContractPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ContractDO>()
            .likeIfPresent(ContractDO::getName, reqVO.getName())
            .eqIfPresent(ContractDO::getCustomerId, reqVO.getCustomerId())
            .eqIfPresent(ContractDO::getBusinessId, reqVO.getBusinessId())
            .betweenIfPresent(ContractDO::getOrderDate, reqVO.getOrderDate())
            .eqIfPresent(ContractDO::getNo, reqVO.getNo())
            .eqIfPresent(ContractDO::getDiscountPercent, reqVO.getDiscountPercent())
            .eqIfPresent(ContractDO::getProductPrice, reqVO.getProductPrice())
            .orderByDesc(ContractDO::getId));
    }

    default List<ContractDO> selectList(ContractExportReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<ContractDO>()
            .likeIfPresent(ContractDO::getName, reqVO.getName())
            .eqIfPresent(ContractDO::getCustomerId, reqVO.getCustomerId())
            .eqIfPresent(ContractDO::getBusinessId, reqVO.getBusinessId())
            .betweenIfPresent(ContractDO::getOrderDate, reqVO.getOrderDate())
            .eqIfPresent(ContractDO::getNo, reqVO.getNo())
            .eqIfPresent(ContractDO::getDiscountPercent, reqVO.getDiscountPercent())
            .eqIfPresent(ContractDO::getProductPrice, reqVO.getProductPrice())
            .orderByDesc(ContractDO::getId));
    }

}
