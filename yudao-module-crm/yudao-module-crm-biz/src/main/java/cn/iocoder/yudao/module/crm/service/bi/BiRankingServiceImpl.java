package cn.iocoder.yudao.module.crm.service.bi;

import cn.iocoder.yudao.framework.security.core.util.SecurityFrameworkUtils;

import cn.iocoder.yudao.module.crm.controller.admin.bi.vo.BiRanKingRespVO;
import cn.iocoder.yudao.module.crm.controller.admin.bi.vo.BiRankingReqVO;
import cn.iocoder.yudao.module.crm.dal.mysql.bi.BiRankingMapper;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.api.dept.dto.DeptRespDTO;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import cn.iocoder.yudao.module.system.api.user.dto.AdminUserRespDTO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service(value = "biRankingService")
@Validated
public class BiRankingServiceImpl implements BiRankingService {

    @Resource
    private BiRankingMapper biRankingMapper;
    @Resource
    private AdminUserApi adminUserApi;
    @Resource
    private DeptApi deptApi;

    @Override
    public List<BiRanKingRespVO> contractRanKing(BiRankingReqVO biRankingReqVO) {
        return processRanking(biRankingReqVO, biRankingMapper::contractRanKing);
    }

    @Override
    public List<BiRanKingRespVO> receivablesRanKing(BiRankingReqVO biRankingReqVO) {
        return processRanking(biRankingReqVO, biRankingMapper::receivablesRanKing);
    }

    /**
     * 处理排行榜
     *
     * @param biRankingReqVO  参数
     * @param rankingFunction 排行榜方法
     * @return List<BiRanKingRespVO>
     */
    private List<BiRanKingRespVO> processRanking(BiRankingReqVO biRankingReqVO, Function<BiRankingReqVO, List<BiRanKingRespVO>> rankingFunction) {
        analyzeAuth(biRankingReqVO);
        if (biRankingReqVO.getUserIds().isEmpty()) {
            return new ArrayList<>();
        }
        List<BiRanKingRespVO> biRanKingRespVOS = rankingFunction.apply(biRankingReqVO);
        return setName(biRanKingRespVOS);
    }

    /**
     * 设置用户名称
     *
     * @param biRanKingRespVOS 排行榜数据
     * @return List<BiRanKingRespVO>
     */
    private List<BiRanKingRespVO> setName(List<BiRanKingRespVO> biRanKingRespVOS) {
        List<Long> userIds = biRanKingRespVOS.stream().map(BiRanKingRespVO::getOwnerUserId).collect(Collectors.toList());
        Map<Long, AdminUserRespDTO> userMap = adminUserApi.getUserMap(userIds);
        Map<Long, DeptRespDTO> deptMap = deptApi.getDeptMap(userMap.values().stream().map(AdminUserRespDTO::getDeptId).collect(Collectors.toList()));
        for (BiRanKingRespVO biRanKingRespVO : biRanKingRespVOS) {
            AdminUserRespDTO adminUserRespDTO = userMap.get(biRanKingRespVO.getOwnerUserId());
            if (adminUserRespDTO != null) {
                biRanKingRespVO.setNickname(adminUserRespDTO.getNickname());
                DeptRespDTO deptRespDTO = deptMap.get(adminUserRespDTO.getDeptId());
                if (deptRespDTO != null) {
                    biRanKingRespVO.setDeptName(deptRespDTO.getName());
                }
            }
        }
        return biRanKingRespVOS;
    }

    /**
     * 分析权限
     *
     * @param biRankingReqVO 参数
     */
    public void analyzeAuth(BiRankingReqVO biRankingReqVO) {
        Long deptId = biRankingReqVO.getDeptId() == null ? adminUserApi.getUser(SecurityFrameworkUtils.getLoginUserId()).getDeptId() : biRankingReqVO.getDeptId();
        List<Long> deptIds = deptApi.getChildDeptList(deptId).stream().map(DeptRespDTO::getId).collect(Collectors.toList());
        deptIds.add(deptId);
        biRankingReqVO.setUserIds(adminUserApi.getUserListByDeptIds(deptIds).stream().map(AdminUserRespDTO::getId).collect(Collectors.toList()));
    }
}