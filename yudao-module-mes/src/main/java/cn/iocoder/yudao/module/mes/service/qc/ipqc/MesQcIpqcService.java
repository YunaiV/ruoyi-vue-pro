package cn.iocoder.yudao.module.mes.service.qc.ipqc;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.mes.controller.admin.qc.ipqc.vo.MesQcIpqcPageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.qc.ipqc.vo.MesQcIpqcSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.qc.defectrecord.MesQcDefectRecordDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.qc.ipqc.MesQcIpqcDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * MES 过程检验单（IPQC） Service 接口
 *
 * @author 芋道源码
 */
public interface MesQcIpqcService {

    /**
     * 创建过程检验单
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createIpqc(@Valid MesQcIpqcSaveReqVO createReqVO);

    /**
     * 更新过程检验单
     *
     * @param updateReqVO 更新信息
     */
    void updateIpqc(@Valid MesQcIpqcSaveReqVO updateReqVO);

    /**
     * 完成过程检验单
     *
     * @param id 编号
     */
    void finishIpqc(Long id);

    /**
     * 删除过程检验单
     *
     * @param id 编号
     */
    void deleteIpqc(Long id);

    /**
     * 获得过程检验单
     *
     * @param id 编号
     * @return 过程检验单
     */
    MesQcIpqcDO getIpqc(Long id);

    /**
     * 获得过程检验单分页
     *
     * @param pageReqVO 分页查询
     * @return 过程检验单分页
     */
    PageResult<MesQcIpqcDO> getIpqcPage(MesQcIpqcPageReqVO pageReqVO);

    /**
     * 校验过程检验单存在
     *
     * @param id 编号
     * @return 过程检验单
     */
    MesQcIpqcDO validateIpqcExists(Long id);

    /**
     * 根据缺陷记录重新计算主表的缺陷统计（含行级下沉）
     *
     * @param ipqcId  过程检验单 ID
     * @param records 缺陷记录列表
     */
    void recalculateDefectStats(Long ipqcId, List<MesQcDefectRecordDO> records);

}
