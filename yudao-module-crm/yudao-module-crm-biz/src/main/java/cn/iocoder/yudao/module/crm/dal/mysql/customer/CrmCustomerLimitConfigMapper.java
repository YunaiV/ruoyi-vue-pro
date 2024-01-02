package cn.iocoder.yudao.module.crm.dal.mysql.customer;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.crm.controller.admin.customer.vo.limitconfig.CrmCustomerLimitConfigCreateReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.customer.vo.limitconfig.CrmCustomerLimitConfigPageReqVO;
import cn.iocoder.yudao.module.crm.dal.dataobject.customer.CrmCustomerLimitConfigDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Arrays;
import java.util.stream.Collectors;

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

    default CrmCustomerLimitConfigDO selectByLimitConfig(CrmCustomerLimitConfigCreateReqVO reqVO){
        LambdaQueryWrapperX<CrmCustomerLimitConfigDO> queryWrapper = new LambdaQueryWrapperX<>();
        queryWrapper.apply("FIND_IN_SET({0}, user_ids) > 0", reqVO.getUserId());
        queryWrapper.eq(CrmCustomerLimitConfigDO::getType, reqVO.getType());
        // 将部门ID列表转换成逗号分隔的字符串
        String deptIdsString = reqVO.getDeptIds().stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
        queryWrapper.apply("FIND_IN_SET({0}, dept_ids) > 0", deptIdsString);
        return selectOne(queryWrapper);
    }

}
