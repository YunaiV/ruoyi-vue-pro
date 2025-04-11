package cn.iocoder.yudao.module.fms.dal.mysql.finance.subject;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.fms.controller.admin.finance.subject.vo.FmsCompanyPageReqVO;
import cn.iocoder.yudao.module.fms.dal.dataobject.finance.subject.FmsCompanyDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * Fms财务公司 Mapper
 *
 * @author 王岽宇
 */
@Mapper
public interface FmsCompanyMapper extends BaseMapperX<FmsCompanyDO> {

    default PageResult<FmsCompanyDO> selectPage(FmsCompanyPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<FmsCompanyDO>()
            .eqIfPresent(FmsCompanyDO::getCreator, reqVO.getCreator())
            .betweenIfPresent(FmsCompanyDO::getCreateTime, reqVO.getCreateTime())
            .eqIfPresent(FmsCompanyDO::getUpdater, reqVO.getUpdater())
            .betweenIfPresent(FmsCompanyDO::getUpdateTime, reqVO.getUpdateTime())
            .likeIfPresent(FmsCompanyDO::getName, reqVO.getName())
            .likeIfPresent(FmsCompanyDO::getContact, reqVO.getContact())
            .likeIfPresent(FmsCompanyDO::getMobile, reqVO.getMobile())
            .likeIfPresent(FmsCompanyDO::getTelephone, reqVO.getTelephone())
            .likeIfPresent(FmsCompanyDO::getEmail, reqVO.getEmail())
            .likeIfPresent(FmsCompanyDO::getFax, reqVO.getFax())
            .likeIfPresent(FmsCompanyDO::getDeliveryAddress, reqVO.getDeliveryAddress())
            .likeIfPresent(FmsCompanyDO::getCompanyAddress, reqVO.getCompanyAddress())
            .likeIfPresent(FmsCompanyDO::getRemark, reqVO.getRemark())
            .eqIfPresent(FmsCompanyDO::getStatus, reqVO.getStatus())
            .likeIfPresent(FmsCompanyDO::getTaxNo, reqVO.getTaxNo())
            .likeIfPresent(FmsCompanyDO::getBankName, reqVO.getBankName())
            .likeIfPresent(FmsCompanyDO::getBankAccount, reqVO.getBankAccount())
            .likeIfPresent(FmsCompanyDO::getBankAddress, reqVO.getBankAddress())
            .orderByDesc(FmsCompanyDO::getId));
    }

    //精简列表
    default List<FmsCompanyDO> selectListSimple() {
        return selectList(new LambdaQueryWrapperX<FmsCompanyDO>()
            .select(FmsCompanyDO::getId, FmsCompanyDO::getName)
            .orderByDesc(FmsCompanyDO::getId));
    }
}