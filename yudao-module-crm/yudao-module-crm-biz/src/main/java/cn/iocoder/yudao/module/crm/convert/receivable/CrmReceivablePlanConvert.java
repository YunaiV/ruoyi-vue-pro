package cn.iocoder.yudao.module.crm.convert.receivable;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.crm.controller.admin.receivable.vo.plan.CrmReceivablePlanCreateReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.receivable.vo.plan.CrmReceivablePlanRespVO;
import cn.iocoder.yudao.module.crm.controller.admin.receivable.vo.plan.CrmReceivablePlanUpdateReqVO;
import cn.iocoder.yudao.module.crm.dal.dataobject.contract.CrmContractDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.customer.CrmCustomerDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.receivable.CrmReceivableDO;
import cn.iocoder.yudao.module.crm.dal.dataobject.receivable.CrmReceivablePlanDO;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.framework.common.util.collection.MapUtils.findAndThen;

/**
 * 回款计划 Convert
 *
 * @author 芋道源码
 */
@Mapper
public interface CrmReceivablePlanConvert {

    CrmReceivablePlanConvert INSTANCE = Mappers.getMapper(CrmReceivablePlanConvert.class);

    CrmReceivablePlanDO convert(CrmReceivablePlanCreateReqVO bean);

    CrmReceivablePlanDO convert(CrmReceivablePlanUpdateReqVO bean);

    CrmReceivablePlanRespVO convert(CrmReceivablePlanDO bean);

    List<CrmReceivablePlanRespVO> convertList(List<CrmReceivablePlanDO> list);

    default PageResult<CrmReceivablePlanRespVO> convertPage(PageResult<CrmReceivablePlanDO> pageResult, Map<Long, AdminUserRespDTO> userMap,
                                                            List<CrmCustomerDO> customerList, List<CrmContractDO> contractList,
                                                            List<CrmReceivableDO> receivableList) {
        return new PageResult<>(converList(pageResult.getList(), userMap, customerList, contractList, receivableList), pageResult.getTotal());
    }

    default List<CrmReceivablePlanRespVO> converList(List<CrmReceivablePlanDO> receivablePlanList, Map<Long, AdminUserRespDTO> userMap,
                                                     List<CrmCustomerDO> customerList, List<CrmContractDO> contractList,
                                                     List<CrmReceivableDO> receivableList) {
        List<CrmReceivablePlanRespVO> result = convertList(receivablePlanList);
        Map<Long, CrmCustomerDO> customerMap = convertMap(customerList, CrmCustomerDO::getId);
        Map<Long, CrmContractDO> contractMap = convertMap(contractList, CrmContractDO::getId);
        Map<Long, CrmReceivableDO> receivableMap = convertMap(receivableList, CrmReceivableDO::getId);
        result.forEach(item -> {
            setUserInfo(item, userMap);
            findAndThen(customerMap, item.getCustomerId(), customer -> item.setCustomerName(customer.getName()));
            findAndThen(contractMap, item.getContractId(), contract -> item.setContractNo(contract.getNo()));
            findAndThen(receivableMap, item.getReceivableId(), receivable -> item.setReturnType(receivable.getReturnType()));
        });
        return result;
    }

    static void setUserInfo(CrmReceivablePlanRespVO receivablePlan, Map<Long, AdminUserRespDTO> userMap) {
        findAndThen(userMap, receivablePlan.getOwnerUserId(), user -> receivablePlan.setOwnerUserName(user.getNickname()));
        findAndThen(userMap, Long.parseLong(receivablePlan.getCreator()), user -> receivablePlan.setCreatorName(user.getNickname()));
    }

}
