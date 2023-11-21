package cn.iocoder.yudao.module.crm.dal.mysql.customerlimitconfig;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.crm.controller.admin.customer.vo.CrmCustomerLimitConfigPageReqVO;
import cn.iocoder.yudao.module.crm.dal.dataobject.customerlimitconfig.CrmCustomerLimitConfigDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 客户限制配置 Mapper
 *
 * @author Wanwan
 */
@Mapper
public interface CrmCustomerLimitConfigMapper extends BaseMapperX<CrmCustomerLimitConfigDO> {

    default PageResult<CrmCustomerLimitConfigDO> selectPage(CrmCustomerLimitConfigPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<CrmCustomerLimitConfigDO>()
                .eqIfPresent(CrmCustomerLimitConfigDO::getType, reqVO.getType())
                .orderByDesc(CrmCustomerLimitConfigDO::getId));
    }

}
