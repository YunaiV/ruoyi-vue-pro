package cn.iocoder.yudao.module.statistics.dal.mysql.infra;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

// TODO @芋艿：api 访问日志，现在会清理，可能要单独有个偏业务的访问表；
/**
 * API 访问日志的统计 Mapper
 *
 * @author owen
 */
@Mapper
public interface ApiAccessLogStatisticsMapper extends BaseMapperX<Object> {

    // TODO @疯狂：是不是 selectIpCount
    Integer selectCountByIp(@Param("beginTime") LocalDateTime beginTime,
                            @Param("endTime") LocalDateTime endTime);

    // TODO @疯狂：是不是 selectUserCount
    Integer selectCountByUserId(@Param("beginTime") LocalDateTime beginTime,
                                @Param("endTime") LocalDateTime endTime);

}
