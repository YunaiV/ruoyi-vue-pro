package cn.iocoder.yudao.module.crm.dal.mysql.customer;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.crm.controller.admin.customer.vo.limitconfig.CrmCustomerLimitConfigPageReqVO;
import cn.iocoder.yudao.module.crm.dal.dataobject.customer.CrmCustomerLimitConfigDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

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

    default List<CrmCustomerLimitConfigDO> selectListByTypeAndUserIdAndDeptId(
            Integer type, Long userId, Long deptId) {
        LambdaQueryWrapperX<CrmCustomerLimitConfigDO> query = new LambdaQueryWrapperX<CrmCustomerLimitConfigDO>()
                .eq(CrmCustomerLimitConfigDO::getType, type);
        query.apply("FIND_IN_SET({0}, user_ids) > 0", userId);
        if (deptId != null) {
            query.apply("FIND_IN_SET({0}, dept_ids) > 0", deptId);
        }
        return selectList(query);
    }

}
