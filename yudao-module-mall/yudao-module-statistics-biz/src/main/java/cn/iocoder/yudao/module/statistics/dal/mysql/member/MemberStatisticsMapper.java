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

    // TODO @芋艿：已经 review
    // TODO @疯狂：要不还是搞下 bo？虽然可能冗余了点~~
    List<MemberAreaStatisticsRespVO> selectSummaryListByAreaId();

    List<MemberSexStatisticsRespVO> selectSummaryListBySex();

    // TODO @芋艿：已经 review
    Integer selectUserCount(@Param("beginTime") LocalDateTime beginTime,
                            @Param("endTime") LocalDateTime endTime);

}
