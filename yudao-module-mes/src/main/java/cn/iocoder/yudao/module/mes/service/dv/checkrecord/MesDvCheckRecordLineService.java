package cn.iocoder.yudao.module.mes.service.dv.checkrecord;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.mes.controller.admin.dv.checkrecord.vo.line.MesDvCheckRecordLinePageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.dv.checkrecord.vo.line.MesDvCheckRecordLineSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.dv.checkrecord.MesDvCheckRecordLineDO;

import jakarta.validation.Valid;
import java.util.List;

/**
 * MES 设备点检记录明细 Service 接口
 *
 * @author 芋道源码
 */
public interface MesDvCheckRecordLineService {

    /**
     * 创建点检记录明细
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createCheckRecordLine(@Valid MesDvCheckRecordLineSaveReqVO createReqVO);

    /**
     * 更新点检记录明细
     *
     * @param updateReqVO 更新信息
     */
    void updateCheckRecordLine(@Valid MesDvCheckRecordLineSaveReqVO updateReqVO);

    /**
     * 删除点检记录明细
     *
     * @param id 编号
     */
    void deleteCheckRecordLine(Long id);

    /**
     * 根据点检记录编号删除所有明细
     *
     * @param recordId 点检记录编号
     */
    void deleteByRecordId(Long recordId);

    /**
     * 批量创建点检记录明细
     *
     * @param lines 明细列表
     */
    void createCheckRecordLineList(List<MesDvCheckRecordLineDO> lines);

    /**
     * 校验点检记录明细存在
     *
     * @param id 编号
     */
    void validateCheckRecordLineExists(Long id);

    /**
     * 获得点检记录明细
     *
     * @param id 编号
     * @return 点检记录明细
     */
    MesDvCheckRecordLineDO getCheckRecordLine(Long id);

    /**
     * 获得点检记录明细分页
     *
     * @param pageReqVO 分页查询
     * @return 点检记录明细分页
     */
    PageResult<MesDvCheckRecordLineDO> getCheckRecordLinePage(MesDvCheckRecordLinePageReqVO pageReqVO);

    /**
     * 获得指定点检记录的所有明细
     *
     * @param recordId 点检记录编号
     * @return 明细列表
     */
    List<MesDvCheckRecordLineDO> getCheckRecordLineListByRecordId(Long recordId);

}
