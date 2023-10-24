package cn.iocoder.yudao.module.crm.dal.mysql.customer;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.crm.controller.admin.customer.vo.CrmCustomerExportReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.customer.vo.CrmCustomerPageReqVO;
import cn.iocoder.yudao.module.crm.dal.dataobject.customer.CrmCustomerDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 客户 Mapper
 *
 * @author Wanwan
 */
@Mapper
public interface CrmCustomerMapper extends BaseMapperX<CrmCustomerDO> {

    default PageResult<CrmCustomerDO> selectPage(CrmCustomerPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<CrmCustomerDO>()
                .likeIfPresent(CrmCustomerDO::getName, reqVO.getName())
                .eqIfPresent(CrmCustomerDO::getMobile, reqVO.getMobile())
                .eqIfPresent(CrmCustomerDO::getTelephone, reqVO.getTelephone())
                .likeIfPresent(CrmCustomerDO::getWebsite, reqVO.getWebsite())
                .orderByDesc(CrmCustomerDO::getId));
    }

    default List<CrmCustomerDO> selectList(CrmCustomerExportReqVO reqVO) {
        return selectList(new LambdaQueryWrapperX<CrmCustomerDO>()
                .likeIfPresent(CrmCustomerDO::getName, reqVO.getName())
                .eqIfPresent(CrmCustomerDO::getMobile, reqVO.getMobile())
                .eqIfPresent(CrmCustomerDO::getTelephone, reqVO.getTelephone())
                .likeIfPresent(CrmCustomerDO::getWebsite, reqVO.getWebsite())
                .orderByDesc(CrmCustomerDO::getId));
    }

}
