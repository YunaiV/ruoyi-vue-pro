package cn.iocoder.yudao.module.statistics.service.member;

import cn.iocoder.yudao.module.statistics.controller.admin.member.vo.MemberAnalyseRespVO;
import cn.iocoder.yudao.module.statistics.controller.admin.member.vo.MemberAreaStatisticsRespVO;
import cn.iocoder.yudao.module.statistics.controller.admin.member.vo.MemberSexStatisticsRespVO;
import cn.iocoder.yudao.module.statistics.controller.admin.member.vo.MemberSummaryRespVO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 会员信息的统计 Service 接口
 *
 * @author owen
 */
public interface MemberStatisticsService {

    /**
     * 获取会员统计
     *
     * @return 会员统计
     */
    MemberSummaryRespVO getMemberSummary();

    /**
     * 按照省份，获得会员统计列表
     *
     * @return 会员统计列表
     */
    List<MemberAreaStatisticsRespVO> getMemberAreaStatisticsList();

    /**
     * 按照性别，获得会员统计列表
     *
     * @return 会员统计列表
     */
    List<MemberSexStatisticsRespVO> getMemberSexStatisticsList();

    /**
     * 获取用户分析数据
     *
     * @param beginTime 起始时间
     * @param endTime   截止时间
     * @return 用户分析数据
     */
    MemberAnalyseRespVO getMemberAnalyse(LocalDateTime beginTime, LocalDateTime endTime);

}
