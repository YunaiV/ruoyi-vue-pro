package cn.iocoder.yudao.module.srm.dal.mysql.finance.subject;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.srm.controller.admin.finance.subject.vo.ErpFinanceSubjectPageReqVO;
import cn.iocoder.yudao.module.srm.dal.dataobject.finance.subject.ErpFinanceSubjectDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * Erp财务主体 Mapper
 *
 * @author 王岽宇
 */
@Mapper
public interface ErpFinanceSubjectMapper extends BaseMapperX<ErpFinanceSubjectDO> {

    default PageResult<ErpFinanceSubjectDO> selectPage(ErpFinanceSubjectPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ErpFinanceSubjectDO>()
            .eqIfPresent(ErpFinanceSubjectDO::getCreator, reqVO.getCreator())
            .betweenIfPresent(ErpFinanceSubjectDO::getCreateTime, reqVO.getCreateTime())
            .eqIfPresent(ErpFinanceSubjectDO::getUpdater, reqVO.getUpdater())
            .betweenIfPresent(ErpFinanceSubjectDO::getUpdateTime, reqVO.getUpdateTime())
            .likeIfPresent(ErpFinanceSubjectDO::getName, reqVO.getName())
            .likeIfPresent(ErpFinanceSubjectDO::getContact, reqVO.getContact())
            .likeIfPresent(ErpFinanceSubjectDO::getMobile, reqVO.getMobile())
            .likeIfPresent(ErpFinanceSubjectDO::getTelephone, reqVO.getTelephone())
            .likeIfPresent(ErpFinanceSubjectDO::getEmail, reqVO.getEmail())
            .likeIfPresent(ErpFinanceSubjectDO::getFax, reqVO.getFax())
            .likeIfPresent(ErpFinanceSubjectDO::getDeliveryAddress, reqVO.getDeliveryAddress())
            .likeIfPresent(ErpFinanceSubjectDO::getCompanyAddress, reqVO.getCompanyAddress())
            .likeIfPresent(ErpFinanceSubjectDO::getRemark, reqVO.getRemark())
            .eqIfPresent(ErpFinanceSubjectDO::getStatus, reqVO.getStatus())
            .likeIfPresent(ErpFinanceSubjectDO::getTaxNo, reqVO.getTaxNo())
            .likeIfPresent(ErpFinanceSubjectDO::getBankName, reqVO.getBankName())
            .likeIfPresent(ErpFinanceSubjectDO::getBankAccount, reqVO.getBankAccount())
            .likeIfPresent(ErpFinanceSubjectDO::getBankAddress, reqVO.getBankAddress())
            .orderByDesc(ErpFinanceSubjectDO::getId));
    }

    //精简列表
    default List<ErpFinanceSubjectDO> selectListSimple() {
        return selectList(new LambdaQueryWrapperX<ErpFinanceSubjectDO>()
            .select(ErpFinanceSubjectDO::getId, ErpFinanceSubjectDO::getName)
            .orderByDesc(ErpFinanceSubjectDO::getId));
    }
}