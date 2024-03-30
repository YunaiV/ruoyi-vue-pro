package cn.iocoder.yudao.module.crm.service.statistics;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.iocoder.yudao.framework.ip.core.Area;
import cn.iocoder.yudao.framework.ip.core.enums.AreaTypeEnum;
import cn.iocoder.yudao.framework.ip.core.utils.AreaUtils;
import cn.iocoder.yudao.module.crm.controller.admin.statistics.vo.customer.CrmStatisticsCustomerReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.statistics.vo.portrait.CrmStatisticCustomerAreaRespVO;
import cn.iocoder.yudao.module.crm.controller.admin.statistics.vo.portrait.CrmStatisticCustomerIndustryRespVO;
import cn.iocoder.yudao.module.crm.controller.admin.statistics.vo.portrait.CrmStatisticCustomerLevelRespVO;
import cn.iocoder.yudao.module.crm.controller.admin.statistics.vo.portrait.CrmStatisticCustomerSourceRespVO;
import cn.iocoder.yudao.module.crm.dal.mysql.statistics.CrmStatisticsPortraitMapper;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import cn.iocoder.yudao.module.system.api.dict.DictDataApi;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;
import static cn.iocoder.yudao.framework.common.util.collection.MapUtils.findAndThen;
import static cn.iocoder.yudao.module.crm.enums.DictTypeConstants.*;

// TODO @puhui999：参考 CrmStatisticsCustomerServiceImpl 代码风格，优化下这个类哈；包括命名、空行、注释等；
/**
 * CRM 客户画像 Service 实现类
 *
 * @author HUIHUI
 */
@Service
public class CrmStatisticsPortraitServiceImpl implements CrmStatisticsPortraitService {

    @Resource
    private CrmStatisticsPortraitMapper portraitMapper;

    @Resource
    private AdminUserApi adminUserApi;
    @Resource
    private DeptApi deptApi;
    @Resource
    private DictDataApi dictDataApi;

    @Override
    public List<CrmStatisticCustomerAreaRespVO> getCustomerAreaSummary(CrmStatisticsCustomerReqVO reqVO) {
        // 1. 获得用户编号数组
        List<Long> userIds = getUserIds(reqVO);
        if (CollUtil.isEmpty(userIds)) {
            return Collections.emptyList();
        }
        reqVO.setUserIds(userIds);
        // 2. 获取客户地区统计数据
        List<CrmStatisticCustomerAreaRespVO> list = portraitMapper.selectSummaryListByAreaId(reqVO);
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }

        // 拼接数据
        List<Area> areaList = AreaUtils.getByType(AreaTypeEnum.PROVINCE, area -> area);
        areaList.add(new Area().setId(null).setName("未知"));
        Map<Integer, Area> areaMap = convertMap(areaList, Area::getId);
        List<CrmStatisticCustomerAreaRespVO> customerAreaRespVOList = convertList(list, item -> {
            Integer parentId = AreaUtils.getParentIdByType(item.getAreaId(), AreaTypeEnum.PROVINCE);
            // TODO @puhui999：找不到，可以归到未知哈；
            if (parentId == null) {
                return item;
            }
            findAndThen(areaMap, parentId, area -> item.setAreaId(parentId).setAreaName(area.getName()));
            return item;
        });
        return customerAreaRespVOList;
    }

    @Override
    public List<CrmStatisticCustomerIndustryRespVO> getCustomerIndustrySummary(CrmStatisticsCustomerReqVO reqVO) {
        // 1. 获得用户编号数组
        List<Long> userIds = getUserIds(reqVO);
        if (CollUtil.isEmpty(userIds)) {
            return Collections.emptyList();
        }
        reqVO.setUserIds(userIds);
        // 2. 获取客户行业统计数据
        List<CrmStatisticCustomerIndustryRespVO> industryRespVOList = portraitMapper.selectCustomerIndustryListGroupbyIndustryId(reqVO);
        if (CollUtil.isEmpty(industryRespVOList)) {
            return Collections.emptyList();
        }

        return convertList(industryRespVOList, item -> {
            if (ObjUtil.isNull(item.getIndustryId())) {
                return item;
            }
            item.setIndustryName(dictDataApi.getDictDataLabel(CRM_CUSTOMER_INDUSTRY, item.getIndustryId()));
            return item;
        });
    }

    @Override
    public List<CrmStatisticCustomerSourceRespVO> getCustomerSourceSummary(CrmStatisticsCustomerReqVO reqVO) {
        // 1. 获得用户编号数组
        List<Long> userIds = getUserIds(reqVO);
        if (CollUtil.isEmpty(userIds)) {
            return Collections.emptyList();
        }
        reqVO.setUserIds(userIds);

        // 2. 获取客户行业统计数据
        List<CrmStatisticCustomerSourceRespVO> sourceRespVOList = portraitMapper.selectCustomerSourceListGroupbySource(reqVO);
        if (CollUtil.isEmpty(sourceRespVOList)) {
            return Collections.emptyList();
        }

        return convertList(sourceRespVOList, item -> {
            if (ObjUtil.isNull(item.getSource())) {
                return item;
            }
            item.setSourceName(dictDataApi.getDictDataLabel(CRM_CUSTOMER_SOURCE, item.getSource()));
            return item;
        });
    }

    @Override
    public List<CrmStatisticCustomerLevelRespVO> getCustomerLevelSummary(CrmStatisticsCustomerReqVO reqVO) {
        // 1. 获得用户编号数组
        List<Long> userIds = getUserIds(reqVO);
        if (CollUtil.isEmpty(userIds)) {
            return Collections.emptyList();
        }
        reqVO.setUserIds(userIds);
        // 2. 获取客户行业统计数据
        List<CrmStatisticCustomerLevelRespVO> levelRespVOList = portraitMapper.selectCustomerLevelListGroupbyLevel(reqVO);
        if (CollUtil.isEmpty(levelRespVOList)) {
            return Collections.emptyList();
        }

        return convertList(levelRespVOList, item -> {
            if (ObjUtil.isNull(item.getLevel())) {
                return item;
            }
            item.setLevelName(dictDataApi.getDictDataLabel(CRM_CUSTOMER_LEVEL, item.getLevel()));
            return item;
        });
    }

    /**
     * 获取用户编号数组。如果用户编号为空, 则获得部门下的用户编号数组，包括子部门的所有用户编号
     *
     * @param reqVO 请求参数
     * @return 用户编号数组
     */
    private List<Long> getUserIds(CrmStatisticsCustomerReqVO reqVO) {
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
