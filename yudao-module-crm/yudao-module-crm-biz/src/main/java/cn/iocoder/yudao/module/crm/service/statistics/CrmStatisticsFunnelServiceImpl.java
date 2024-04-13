package cn.iocoder.yudao.module.crm.service.statistics;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.module.crm.controller.admin.statistics.vo.funnel.CrmStatisticBusinessEndStatusRespVO;
import cn.iocoder.yudao.module.crm.controller.admin.statistics.vo.funnel.CrmStatisticFunnelRespVO;
import cn.iocoder.yudao.module.crm.controller.admin.statistics.vo.funnel.CrmStatisticsFunnelReqVO;
import cn.iocoder.yudao.module.crm.dal.dataobject.business.CrmBusinessDO;
import cn.iocoder.yudao.module.crm.dal.mysql.statistics.CrmStatisticsFunnelMapper;
import cn.iocoder.yudao.module.crm.enums.business.CrmBusinessEndStatusEnum;
import cn.iocoder.yudao.module.crm.service.business.CrmBusinessService;
import cn.iocoder.yudao.module.crm.service.customer.CrmCustomerService;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.*;

/**
 * CRM 销售漏斗分析 Service 实现类
 *
 * @author HUIHUI
 */
@Service
public class CrmStatisticsFunnelServiceImpl implements CrmStatisticsFunnelService {

    @Resource
    private CrmStatisticsFunnelMapper funnelMapper;

    @Resource
    private AdminUserApi adminUserApi;
    @Resource
    private CrmCustomerService customerService;
    @Resource
    private CrmBusinessService businessService;
    @Resource
    private DeptApi deptApi;

    @Override
    public CrmStatisticFunnelRespVO getFunnelSummary(CrmStatisticsFunnelReqVO reqVO) {
        // 1. 获得用户编号数组
        List<Long> userIds = getUserIds(reqVO);
        if (CollUtil.isEmpty(userIds)) {
            return null;
        }
        reqVO.setUserIds(userIds);

        // 2. 获得漏斗数据
        return new CrmStatisticFunnelRespVO(
                customerService.getCustomerCountByOwnerUserIds(userIds, reqVO.getTimes()),
                businessService.getBusinessCountByOwnerUserIdsAndEndStatus(userIds, reqVO.getTimes(), null),
                businessService.getBusinessCountByOwnerUserIdsAndEndStatus(userIds, reqVO.getTimes(), CrmBusinessEndStatusEnum.WIN.getStatus())
        );
    }

    @Override
    public List<CrmStatisticBusinessEndStatusRespVO> getBusinessEndStatusSummary(CrmStatisticsFunnelReqVO reqVO) {
        // 1. 获得用户编号数组
        List<Long> userIds = getUserIds(reqVO);
        if (CollUtil.isEmpty(userIds)) {
            return null;
        }
        reqVO.setUserIds(userIds);

        // 2.1 获得用户负责的商机
        List<CrmBusinessDO> businessList = businessService.getBusinessListByOwnerUserIdsAndEndStatusNotNull(userIds, reqVO.getTimes());
        // 2.2 统计各阶段数据
        Map<Integer, List<CrmBusinessDO>> businessMap = convertMultiMap(businessList, CrmBusinessDO::getEndStatus);
        return convertList(CrmBusinessEndStatusEnum.values(), endStatusEnum -> {
            List<CrmBusinessDO> list = businessMap.get(endStatusEnum.getStatus());
            if (CollUtil.isEmpty(list)) {
                return new CrmStatisticBusinessEndStatusRespVO(endStatusEnum.getStatus(), 0L, BigDecimal.ZERO);
            }
            return new CrmStatisticBusinessEndStatusRespVO(endStatusEnum.getStatus(), (long) list.size(),
                    getSumValue(list, CrmBusinessDO::getTotalPrice, BigDecimal::add));
        });
    }

    /**
     * 获取用户编号数组。如果用户编号为空, 则获得部门下的用户编号数组，包括子部门的所有用户编号
     *
     * @param reqVO 请求参数
     * @return 用户编号数组
     */
    private List<Long> getUserIds(CrmStatisticsFunnelReqVO reqVO) {
        // 情况一：选中某个用户
        if (ObjUtil.isNotNull(reqVO.getUserId())) {
            return List.of(reqVO.getUserId());
        }
        // 情况二：选中某个部门
        // 2.1 获得部门列表
        List<Long> deptIds = convertList(deptApi.getChildDeptList(reqVO.getDeptId()), DeptRespDTO::getId);
        deptIds.add(reqVO.getDeptId());
        // 2.2 获得用户编号
        return convertList(adminUserApi.getUserListByDeptIds(deptIds), AdminUserRespDTO::getId);
    }

}
