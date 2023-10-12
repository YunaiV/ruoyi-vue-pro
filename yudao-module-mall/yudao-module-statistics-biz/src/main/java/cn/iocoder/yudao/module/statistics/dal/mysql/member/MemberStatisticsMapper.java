package cn.iocoder.yudao.module.statistics.dal.mysql.member;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.statistics.controller.admin.member.vo.MemberAreaStatisticsRespVO;
import cn.iocoder.yudao.module.statistics.controller.admin.member.vo.MemberSexStatisticsRespVO;
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
public interface MemberStatisticsMapper extends BaseMapperX<Object> {

    List<MemberAreaStatisticsRespVO> selectSummaryListByAreaId();

    List<MemberSexStatisticsRespVO> selectSummaryListBySex();

    Integer selectUserCount(@Param("beginTime") LocalDateTime beginTime,
                            @Param("endTime") LocalDateTime endTime);

}
