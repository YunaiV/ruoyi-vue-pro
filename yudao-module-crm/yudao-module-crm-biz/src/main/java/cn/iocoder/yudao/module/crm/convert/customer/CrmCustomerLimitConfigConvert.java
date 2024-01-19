package cn.iocoder.yudao.module.crm.convert.customer;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.collection.CollectionUtils;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.crm.controller.admin.customer.vo.limitconfig.CrmCustomerLimitConfigRespVO;
import cn.iocoder.yudao.module.crm.dal.dataobject.customer.CrmCustomerLimitConfigDO;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Map;

/**
 * 客户限制配置 Convert
 *
 * @author Wanwan
 */
@Mapper
public interface CrmCustomerLimitConfigConvert {

    CrmCustomerLimitConfigConvert INSTANCE = Mappers.getMapper(CrmCustomerLimitConfigConvert.class);

    default PageResult<CrmCustomerLimitConfigRespVO> convertPage(
            PageResult<CrmCustomerLimitConfigDO> pageResult,
            Map<Long, AdminUserRespDTO> userMap, Map<Long, DeptRespDTO> deptMap) {
        List<CrmCustomerLimitConfigRespVO> list = CollectionUtils.convertList(pageResult.getList(),
                limitConfig -> convert(limitConfig, userMap, deptMap));
        return new PageResult<>(list, pageResult.getTotal());
    }

    default CrmCustomerLimitConfigRespVO convert(CrmCustomerLimitConfigDO limitConfig,
                                                 Map<Long, AdminUserRespDTO> userMap, Map<Long, DeptRespDTO> deptMap) {
        CrmCustomerLimitConfigRespVO limitConfigVO = BeanUtils.toBean(limitConfig, CrmCustomerLimitConfigRespVO.class);
        limitConfigVO.setUsers(CollectionUtils.convertList(limitConfigVO.getUserIds(), userMap::get));
        limitConfigVO.setDepts(CollectionUtils.convertList(limitConfigVO.getDeptIds(), deptMap::get));
        return limitConfigVO;
    }

}
