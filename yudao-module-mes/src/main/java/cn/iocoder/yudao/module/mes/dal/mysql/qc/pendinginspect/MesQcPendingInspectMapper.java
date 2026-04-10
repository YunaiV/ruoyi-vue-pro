package cn.iocoder.yudao.module.mes.dal.mysql.qc.pendinginspect;

import cn.iocoder.yudao.module.mes.controller.admin.qc.pendinginspect.vo.MesQcPendingInspectRespVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * MES 待检任务 Mapper
 *
 * 使用 UNION ALL SQL 从多个来源表（到货通知单、外协入库单等）查询待检行
 */
@Mapper
public interface MesQcPendingInspectMapper {

    IPage<MesQcPendingInspectRespVO> selectQcPendingPage(IPage<MesQcPendingInspectRespVO> page,
                                                         @Param("sourceDocCode") String sourceDocCode,
                                                         @Param("qcType") Integer qcType,
                                                         @Param("itemId") Long itemId);

}
