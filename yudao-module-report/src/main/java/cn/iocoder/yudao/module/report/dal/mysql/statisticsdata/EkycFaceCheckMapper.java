package cn.iocoder.yudao.module.report.dal.mysql.statisticsdata;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.module.report.controller.admin.ekycfacecheck.vo.EkycChannelStatusCountDTO;
import cn.iocoder.yudao.module.report.dal.dataobject.statisticsdata.EkycFaceCheckDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

/**
 * 通用统计数据 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface EkycFaceCheckMapper extends BaseMapperX<EkycFaceCheckDO> {


    @Select("""
            SELECT create_time as date, status, COUNT(*) AS count
              FROM report_ekyc_face_check
              WHERE  create_time BETWEEN #{startDate} AND #{endDate}
            GROUP BY create_time, status
            """)
    List<EkycChannelStatusCountDTO> selectChannelStatusSummary(@Param("startDate") LocalDate startDate,
                                                               @Param("endDate") LocalDate endDate);
}
