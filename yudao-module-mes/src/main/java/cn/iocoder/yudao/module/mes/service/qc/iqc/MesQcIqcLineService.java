package cn.iocoder.yudao.module.mes.service.qc.iqc;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.mes.controller.admin.qc.iqc.vo.line.MesQcIqcLinePageReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.qc.iqc.MesQcIqcLineDO;

import cn.iocoder.yudao.module.mes.dal.dataobject.qc.defectrecord.MesQcDefectRecordDO;

import java.util.List;

/**
 * MES 来料检验单行 Service 接口
 *
 * @author 芋道源码
 */
public interface MesQcIqcLineService {

    /**
     * 校验来料检验行存在
     *
     * @param id 编号
     * @return 来料检验行
     */
    MesQcIqcLineDO validateIqcLineExists(Long id);

    /**
     * 获得来料检验行
     *
     * @param id 编号
     * @return 来料检验行
     */
    MesQcIqcLineDO getIqcLine(Long id);

    /**
     * 获得来料检验行分页
     *
     * @param pageReqVO 分页查询
     * @return 来料检验行分页
     */
    PageResult<MesQcIqcLineDO> getIqcLinePage(MesQcIqcLinePageReqVO pageReqVO);

    /**
     * 从模板指标自动生成检验行
     *
     * @param iqcId 来料检验单 ID
     * @param templateId 模板 ID
     */
    void createLinesFromTemplate(Long iqcId, Long templateId);

    /**
     * 根据缺陷记录重新计算各行的缺陷统计数量
     *
     * @param iqcId   来料检验单 ID
     * @param records 缺陷记录列表
     */
    void recalculateLineDefectStats(Long iqcId, List<MesQcDefectRecordDO> records);

    /**
     * 根据来料检验单 ID 获取所有行
     *
     * @param iqcId 来料检验单 ID
     * @return 行列表
     */
    List<MesQcIqcLineDO> getIqcLineListByIqcId(Long iqcId);

    /**
     * 根据来料检验单 ID 级联删除所有行
     *
     * @param iqcId 来料检验单 ID
     */
    void deleteListByIqcId(Long iqcId);

    /**
     * 统计使用指定计量单位的来料检验行数量
     *
     * @param unitMeasureId 计量单位编号
     * @return 引用数量
     */
    Long getIqcLineCountByUnitMeasureId(Long unitMeasureId);

}
