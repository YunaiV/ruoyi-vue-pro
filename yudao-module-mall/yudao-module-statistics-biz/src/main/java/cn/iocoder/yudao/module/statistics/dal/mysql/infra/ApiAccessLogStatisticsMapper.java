package cn.iocoder.yudao.module.statistics.dal.mysql.infra;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

/**
 * API 访问日志的统计 Mapper
 *
 * @author owen
 */
@Mapper
public interface ApiAccessLogStatisticsMapper extends BaseMapperX<Object> {

    Integer selectCountByIp(@Param("beginTime") LocalDateTime beginTime,
                            @Param("endTime") LocalDateTime endTime);

    Integer selectCountByUserId(@Param("beginTime") LocalDateTime beginTime,
                                @Param("endTime") LocalDateTime endTime);

}
