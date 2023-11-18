package cn.iocoder.yudao.module.crm.convert.customer;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.crm.controller.admin.customer.vo.CrmCustomerLimitConfigCreateReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.customer.vo.CrmCustomerLimitConfigRespVO;
import cn.iocoder.yudao.module.crm.controller.admin.customer.vo.CrmCustomerLimitConfigUpdateReqVO;
import cn.iocoder.yudao.module.crm.dal.dataobject.customerlimitconfig.CrmCustomerLimitConfigDO;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 客户限制配置 Convert
 *
 * @author Wanwan
 */
@Mapper
public interface CrmCustomerLimitConfigConvert {

    CrmCustomerLimitConfigConvert INSTANCE = Mappers.getMapper(CrmCustomerLimitConfigConvert.class);

    CrmCustomerLimitConfigDO convert(CrmCustomerLimitConfigCreateReqVO bean);

    CrmCustomerLimitConfigDO convert(CrmCustomerLimitConfigUpdateReqVO bean);

    CrmCustomerLimitConfigRespVO convert(CrmCustomerLimitConfigDO bean);

    List<CrmCustomerLimitConfigRespVO> convertList(List<CrmCustomerLimitConfigDO> list);

    PageResult<CrmCustomerLimitConfigRespVO> convertPage(PageResult<CrmCustomerLimitConfigDO> page);

    default PageResult<CrmCustomerLimitConfigRespVO> convertPage(PageResult<CrmCustomerLimitConfigDO> pageResult,
                                                                 Map<Long, AdminUserRespDTO> userMap, Map<Long, DeptRespDTO> deptMap) {
        PageResult<CrmCustomerLimitConfigRespVO> result = convertPage(pageResult);
        result.getList().forEach(respVo -> fillNameField(userMap, deptMap, respVo));
        return result;
    }

    default CrmCustomerLimitConfigRespVO convert(CrmCustomerLimitConfigDO customerLimitConfig,
                                                 Map<Long, AdminUserRespDTO> userMap, Map<Long, DeptRespDTO> deptMap) {
        CrmCustomerLimitConfigRespVO respVo = convert(customerLimitConfig);
        fillNameField(userMap, deptMap, respVo);
        return respVo;
    }

    /**
     * 填充名称字段
     *
     * @param userMap 用户映射
     * @param deptMap 部门映射
     * @param respVo 响应实体
     */
    static void fillNameField(Map<Long, AdminUserRespDTO> userMap, Map<Long, DeptRespDTO> deptMap, CrmCustomerLimitConfigRespVO respVo) {
        // TODO wanwan：返回 list，具体怎么拼接叫给前端；
        respVo.setUserNames(respVo.getUserIds().stream().map(userMap::get)
                .filter(Objects::nonNull).map(AdminUserRespDTO::getNickname).collect(Collectors.joining("，")));
        respVo.setDeptNames(respVo.getDeptIds().stream().map(deptMap::get)
                .filter(Objects::nonNull).map(DeptRespDTO::getName).collect(Collectors.joining("，")));
    }

}
