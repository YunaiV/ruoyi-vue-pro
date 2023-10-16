package cn.iocoder.yudao.module.statistics.dal.mysql.member;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.statistics.controller.admin.member.vo.MemberRegisterCountRespVO;
import cn.iocoder.yudao.module.statistics.controller.admin.member.vo.MemberSexStatisticsRespVO;
import cn.iocoder.yudao.module.statistics.controller.admin.member.vo.MemberTerminalStatisticsRespVO;
import cn.iocoder.yudao.module.statistics.service.member.bo.MemberAreaStatisticsRespBO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 会员信息的统计 Mapper
 *
 * @author owen
 */
@Mapper
@SuppressWarnings("rawtypes")
public interface MemberStatisticsMapper extends BaseMapperX {

    // TODO @芋艿：已经 review
    List<MemberAreaStatisticsRespBO> selectSummaryListByAreaId();

    // TODO @芋艿：已经 review
    List<MemberSexStatisticsRespVO> selectSummaryListBySex();

    // TODO @芋艿：已经 review
    List<MemberTerminalStatisticsRespVO> selectSummaryListByRegisterTerminal();

    // TODO @芋艿：已经 review
    Integer selectUserCount(@Param("beginTime") LocalDateTime beginTime,
                            @Param("endTime") LocalDateTime endTime);

    // TODO @芋艿：已经 review
    /**
     * 获得用户的每天注册数量列表
     *
     * @param beginTime 开始时间
     * @param endTime 结束时间
     * @return 每天注册数量列表
     */
    List<MemberRegisterCountRespVO> selectListByCreateTimeBetween(@Param("beginTime") LocalDateTime beginTime,
                                                                  @Param("endTime") LocalDateTime endTime);

}
