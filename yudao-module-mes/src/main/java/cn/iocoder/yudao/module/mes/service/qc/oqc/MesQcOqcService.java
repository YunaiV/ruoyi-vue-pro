package cn.iocoder.yudao.module.mes.service.qc.oqc;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.mes.controller.admin.qc.oqc.vo.MesQcOqcPageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.qc.oqc.vo.MesQcOqcSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.qc.defectrecord.MesQcDefectRecordDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.qc.oqc.MesQcOqcDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * MES 出货检验单（OQC） Service 接口
 *
 * @author 芋道源码
 */
public interface MesQcOqcService {

    /**
     * 创建出货检验单
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createOqc(@Valid MesQcOqcSaveReqVO createReqVO);

    /**
     * 更新出货检验单
     *
     * @param updateReqVO 更新信息
     */
    void updateOqc(@Valid MesQcOqcSaveReqVO updateReqVO);

    /**
     * 完成出货检验单
     *
     * @param id 编号
     */
    void finishOqc(Long id);

    /**
     * 删除出货检验单
     *
     * @param id 编号
     */
    void deleteOqc(Long id);

    /**
     * 校验出货检验单存在
     *
     * @param id 编号
     * @return 出货检验单
     */
    MesQcOqcDO validateOqcExists(Long id);

    /**
     * 获得出货检验单
     *
     * @param id 编号
     * @return 出货检验单
     */
    MesQcOqcDO getOqc(Long id);

    /**
     * 获得出货检验单分页
     *
     * @param pageReqVO 分页查询
     * @return 出货检验单分页
     */
    PageResult<MesQcOqcDO> getOqcPage(MesQcOqcPageReqVO pageReqVO);

    /**
     * 根据缺陷记录重新计算主表的缺陷统计（含行级下沉）
     *
     * @param oqcId   出货检验单 ID
     * @param records 缺陷记录列表
     */
    void recalculateDefectStats(Long oqcId, List<MesQcDefectRecordDO> records);

}
