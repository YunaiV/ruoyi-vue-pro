package cn.iocoder.yudao.module.mes.service.qc.oqc;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.mes.controller.admin.qc.oqc.vo.line.MesQcOqcLinePageReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.qc.defectrecord.MesQcDefectRecordDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.qc.oqc.MesQcOqcLineDO;

import java.util.List;

/**
 * MES 出货检验单行 Service 接口
 *
 * @author 芋道源码
 */
public interface MesQcOqcLineService {

    /**
     * 获得出货检验行
     *
     * @param id 编号
     * @return 出货检验行
     */
    MesQcOqcLineDO getOqcLine(Long id);

    /**
     * 获得出货检验行分页
     *
     * @param pageReqVO 分页查询
     * @return 出货检验行分页
     */
    PageResult<MesQcOqcLineDO> getOqcLinePage(MesQcOqcLinePageReqVO pageReqVO);

    /**
     * 根据出货检验单 ID 查询所有行
     *
     * @param oqcId 出货检验单 ID
     * @return 行列表
     */
    List<MesQcOqcLineDO> getOqcLineListByOqcId(Long oqcId);

    /**
     * 从模板指标自动生成检验行
     *
     * @param oqcId      出货检验单 ID
     * @param templateId 模板 ID
     */
    void createLinesFromTemplate(Long oqcId, Long templateId);

    /**
     * 根据出货检验单 ID 级联删除所有行
     *
     * @param oqcId 出货检验单 ID
     */
    void deleteByOqcId(Long oqcId);

    /**
     * 校验出货检验行存在
     *
     * @param id 编号
     * @return 出货检验行
     */
    MesQcOqcLineDO validateOqcLineExists(Long id);

    /**
     * 根据缺陷记录重新计算行级缺陷统计
     *
     * @param oqcId   出货检验单 ID
     * @param records 缺陷记录列表
     */
    void recalculateLineDefectStats(Long oqcId, List<MesQcDefectRecordDO> records);

}
