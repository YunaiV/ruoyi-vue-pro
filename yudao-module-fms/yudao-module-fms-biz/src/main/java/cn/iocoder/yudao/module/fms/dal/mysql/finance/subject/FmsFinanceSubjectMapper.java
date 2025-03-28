package cn.iocoder.yudao.module.fms.dal.mysql.finance.subject;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.fms.controller.admin.finance.subject.vo.FmsFinanceSubjectPageReqVO;
import cn.iocoder.yudao.module.fms.dal.dataobject.finance.subject.FmsFinanceSubjectDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * Erp财务主体 Mapper
 *
 * @author 王岽宇
 */
@Mapper
public interface FmsFinanceSubjectMapper extends BaseMapperX<FmsFinanceSubjectDO> {

    default PageResult<FmsFinanceSubjectDO> selectPage(FmsFinanceSubjectPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<FmsFinanceSubjectDO>()
            .eqIfPresent(FmsFinanceSubjectDO::getCreator, reqVO.getCreator())
            .betweenIfPresent(FmsFinanceSubjectDO::getCreateTime, reqVO.getCreateTime())
            .eqIfPresent(FmsFinanceSubjectDO::getUpdater, reqVO.getUpdater())
            .betweenIfPresent(FmsFinanceSubjectDO::getUpdateTime, reqVO.getUpdateTime())
            .likeIfPresent(FmsFinanceSubjectDO::getName, reqVO.getName())
            .likeIfPresent(FmsFinanceSubjectDO::getContact, reqVO.getContact())
            .likeIfPresent(FmsFinanceSubjectDO::getMobile, reqVO.getMobile())
            .likeIfPresent(FmsFinanceSubjectDO::getTelephone, reqVO.getTelephone())
            .likeIfPresent(FmsFinanceSubjectDO::getEmail, reqVO.getEmail())
            .likeIfPresent(FmsFinanceSubjectDO::getFax, reqVO.getFax())
            .likeIfPresent(FmsFinanceSubjectDO::getDeliveryAddress, reqVO.getDeliveryAddress())
            .likeIfPresent(FmsFinanceSubjectDO::getCompanyAddress, reqVO.getCompanyAddress())
            .likeIfPresent(FmsFinanceSubjectDO::getRemark, reqVO.getRemark())
            .eqIfPresent(FmsFinanceSubjectDO::getStatus, reqVO.getStatus())
            .likeIfPresent(FmsFinanceSubjectDO::getTaxNo, reqVO.getTaxNo())
            .likeIfPresent(FmsFinanceSubjectDO::getBankName, reqVO.getBankName())
            .likeIfPresent(FmsFinanceSubjectDO::getBankAccount, reqVO.getBankAccount())
            .likeIfPresent(FmsFinanceSubjectDO::getBankAddress, reqVO.getBankAddress())
            .orderByDesc(FmsFinanceSubjectDO::getId));
    }

    //精简列表
    default List<FmsFinanceSubjectDO> selectListSimple() {
        return selectList(new LambdaQueryWrapperX<FmsFinanceSubjectDO>()
            .select(FmsFinanceSubjectDO::getId, FmsFinanceSubjectDO::getName)
            .orderByDesc(FmsFinanceSubjectDO::getId));
    }
}