package cn.iocoder.yudao.module.ai1.dal.mysql.home;

import cn.iocoder.yudao.module.ai1.controller.admin.home.vo.Ai1HomeMessageShareRespVO;
import cn.iocoder.yudao.module.ai1.controller.admin.home.vo.Ai1HomeMessageTrendRespVO;
import cn.iocoder.yudao.module.ai1.controller.admin.home.vo.Ai1HomeSummaryRespVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * AI1 首页统计 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface Ai1HomeMapper {

    Ai1HomeSummaryRespVO selectSummary();

    /**
     * 按天统计消息数量，仅返回有消息的日期
     */
    List<Ai1HomeMessageTrendRespVO> selectMessageTrendListByCreateTimeBetween(@Param("beginTime") LocalDateTime beginTime,
                                                                              @Param("endTime") LocalDateTime endTime);

    /**
     * 按 Agent 统计消息数量，按数量倒序，数量相同按 Agent 编号升序
     */
    List<Ai1HomeMessageShareRespVO> selectMessageShareListByCreateTimeBetween(@Param("beginTime") LocalDateTime beginTime,
                                                                              @Param("endTime") LocalDateTime endTime);

}
