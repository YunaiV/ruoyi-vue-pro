package cn.iocoder.yudao.module.mes.service.qc.rqc;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.mes.controller.admin.qc.rqc.vo.MesQcRqcPageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.qc.rqc.vo.MesQcRqcSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.qc.defectrecord.MesQcDefectRecordDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.qc.rqc.MesQcRqcDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * MES 退货检验单（RQC） Service 接口
 *
 * @author 芋道源码
 */
public interface MesQcRqcService {

    /**
     * 创建退货检验单
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createRqc(@Valid MesQcRqcSaveReqVO createReqVO);

    /**
     * 更新退货检验单
     *
     * @param updateReqVO 更新信息
     */
    void updateRqc(@Valid MesQcRqcSaveReqVO updateReqVO);

    /**
     * 完成退货检验单
     *
     * @param id 编号
     */
    void finishRqc(Long id);

    /**
     * 删除退货检验单
     *
     * @param id 编号
     */
    void deleteRqc(Long id);

    /**
     * 校验退货检验单存在
     *
     * @param id 编号
     * @return 退货检验单
     */
    MesQcRqcDO validateRqcExists(Long id);

    /**
     * 获得退货检验单
     *
     * @param id 编号
     * @return 退货检验单
     */
    MesQcRqcDO getRqc(Long id);

    /**
     * 获得退货检验单分页
     *
     * @param pageReqVO 分页查询
     * @return 退货检验单分页
     */
    PageResult<MesQcRqcDO> getRqcPage(MesQcRqcPageReqVO pageReqVO);

    /**
     * 根据缺陷记录重新计算行级缺陷统计
     *
     * @param rqcId   退货检验单 ID
     * @param records 缺陷记录列表
     */
    void recalculateDefectStats(Long rqcId, List<MesQcDefectRecordDO> records);

}
