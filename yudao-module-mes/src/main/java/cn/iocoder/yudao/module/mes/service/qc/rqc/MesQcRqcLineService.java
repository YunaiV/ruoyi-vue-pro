package cn.iocoder.yudao.module.mes.service.qc.rqc;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.mes.controller.admin.qc.rqc.vo.line.MesQcRqcLinePageReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.qc.defectrecord.MesQcDefectRecordDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.qc.rqc.MesQcRqcLineDO;

import java.util.List;

/**
 * MES 退货检验行 Service 接口
 *
 * @author 芋道源码
 */
public interface MesQcRqcLineService {

    /**
     * 校验退货检验行存在
     *
     * @param id 编号
     * @return 退货检验行
     */
    MesQcRqcLineDO validateRqcLineExists(Long id);

    /**
     * 获得退货检验行
     *
     * @param id 编号
     * @return 退货检验行
     */
    MesQcRqcLineDO getRqcLine(Long id);

    /**
     * 获得退货检验行分页
     *
     * @param pageReqVO 分页查询
     * @return 退货检验行分页
     */
    PageResult<MesQcRqcLineDO> getRqcLinePage(MesQcRqcLinePageReqVO pageReqVO);

    /**
     * 从模板指标自动生成检验行
     *
     * @param rqcId      退货检验单 ID
     * @param templateId 模板 ID
     */
    void createLinesFromTemplate(Long rqcId, Long templateId);

    /**
     * 根据缺陷记录重新计算各行的缺陷统计数量
     *
     * @param rqcId   退货检验单 ID
     * @param records 缺陷记录列表
     */
    void recalculateLineDefectStats(Long rqcId, List<MesQcDefectRecordDO> records);

    /**
     * 根据退货检验单 ID 获取所有行
     *
     * @param rqcId 退货检验单 ID
     * @return 行列表
     */
    List<MesQcRqcLineDO> getRqcLineListByRqcId(Long rqcId);

    /**
     * 根据退货检验单 ID 级联删除所有行
     *
     * @param rqcId 退货检验单 ID
     */
    void deleteByRqcId(Long rqcId);

    /**
     * 统计使用指定计量单位的退货检验行数量
     *
     * @param unitMeasureId 计量单位编号
     * @return 引用数量
     */
    Long getRqcLineCountByUnitMeasureId(Long unitMeasureId);

}
