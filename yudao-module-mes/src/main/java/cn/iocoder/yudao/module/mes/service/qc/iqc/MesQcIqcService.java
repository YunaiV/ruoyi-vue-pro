package cn.iocoder.yudao.module.mes.service.qc.iqc;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.mes.controller.admin.qc.iqc.vo.MesQcIqcPageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.qc.iqc.vo.MesQcIqcSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.qc.defectrecord.MesQcDefectRecordDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.qc.iqc.MesQcIqcDO;
import jakarta.validation.Valid;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * MES 来料检验单（IQC） Service 接口
 *
 * @author 芋道源码
 */
public interface MesQcIqcService {

    /**
     * 创建来料检验单
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createIqc(@Valid MesQcIqcSaveReqVO createReqVO);

    /**
     * 更新来料检验单
     *
     * @param updateReqVO 更新信息
     */
    void updateIqc(@Valid MesQcIqcSaveReqVO updateReqVO);

    /**
     * 完成来料检验单
     *
     * @param id 编号
     */
    void finishIqc(Long id);

    /**
     * 删除来料检验单
     *
     * @param id 编号
     */
    void deleteIqc(Long id);

    /**
     * 校验来料检验单存在
     *
     * @param id 编号
     * @return 来料检验单
     */
    MesQcIqcDO validateIqcExists(Long id);

    /**
     * 获得来料检验单
     *
     * @param id 编号
     * @return 来料检验单
     */
    MesQcIqcDO getIqc(Long id);

    /**
     * 获得来料检验单分页
     *
     * @param pageReqVO 分页查询
     * @return 来料检验单分页
     */
    PageResult<MesQcIqcDO> getIqcPage(MesQcIqcPageReqVO pageReqVO);

    /**
     * 根据缺陷记录重新计算主表的缺陷统计（含行级下沉）
     *
     * @param iqcId   来料检验单 ID
     * @param records 缺陷记录列表
     */
    void recalculateDefectStats(Long iqcId, List<MesQcDefectRecordDO> records);

    /**
     * 批量获取来料检验单 Map
     *
     * @param ids 编号集合
     * @return 来料检验单 Map
     */
    Map<Long, MesQcIqcDO> getIqcMap(Collection<Long> ids);

}
