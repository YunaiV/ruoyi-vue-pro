package cn.iocoder.yudao.module.mes.service.qc.ipqc;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.mes.controller.admin.qc.ipqc.vo.line.MesQcIpqcLinePageReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.qc.defectrecord.MesQcDefectRecordDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.qc.ipqc.MesQcIpqcLineDO;

import java.util.List;

/**
 * MES 过程检验单行 Service 接口
 *
 * @author 芋道源码
 */
public interface MesQcIpqcLineService {

    /**
     * 校验过程检验行存在
     *
     * @param id 编号
     * @return 过程检验行
     */
    MesQcIpqcLineDO validateIpqcLineExists(Long id);

    /**
     * 获得过程检验行
     *
     * @param id 编号
     * @return 过程检验行
     */
    MesQcIpqcLineDO getIpqcLine(Long id);

    /**
     * 获得过程检验行分页
     *
     * @param pageReqVO 分页查询
     * @return 过程检验行分页
     */
    PageResult<MesQcIpqcLineDO> getIpqcLinePage(MesQcIpqcLinePageReqVO pageReqVO);

    /**
     * 从模板指标自动生成检验行
     *
     * @param ipqcId 过程检验单 ID
     * @param templateId 模板 ID
     */
    void createLinesFromTemplate(Long ipqcId, Long templateId);

    /**
     * 根据缺陷记录重新计算行级缺陷统计
     *
     * @param ipqcId  过程检验单 ID
     * @param records 缺陷记录列表
     */
    void recalculateLineDefectStats(Long ipqcId, List<MesQcDefectRecordDO> records);

    /**
     * 根据过程检验单 ID 获取所有行
     *
     * @param ipqcId 过程检验单 ID
     * @return 行列表
     */
    List<MesQcIpqcLineDO> getIpqcLineListByIpqcId(Long ipqcId);

    /**
     * 根据过程检验单 ID 级联删除所有行
     *
     * @param ipqcId 过程检验单 ID
     */
    void deleteListByIpqcId(Long ipqcId);

}
