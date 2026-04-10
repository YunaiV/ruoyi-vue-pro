package cn.iocoder.yudao.module.mes.service.dv.maintenrecord;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.mes.controller.admin.dv.maintenrecord.vo.line.MesDvMaintenRecordLinePageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.dv.maintenrecord.vo.line.MesDvMaintenRecordLineSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.dv.maintenrecord.MesDvMaintenRecordLineDO;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 设备保养记录明细 Service 接口
 *
 * @author 芋道源码
 */
public interface MesDvMaintenRecordLineService {

    /**
     * 创建设备保养记录明细
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createMaintenRecordLine(@Valid MesDvMaintenRecordLineSaveReqVO createReqVO);

    /**
     * 更新设备保养记录明细
     *
     * @param updateReqVO 更新信息
     */
    void updateMaintenRecordLine(@Valid MesDvMaintenRecordLineSaveReqVO updateReqVO);

    /**
     * 删除设备保养记录明细
     *
     * @param id 编号
     */
    void deleteMaintenRecordLine(Long id);

    /**
     * 获得设备保养记录明细
     *
     * @param id 编号
     * @return 设备保养记录明细
     */
    MesDvMaintenRecordLineDO getMaintenRecordLine(Long id);

    /**
     * 获得设备保养记录明细分页
     *
     * @param pageReqVO 分页查询
     * @return 设备保养记录明细分页
     */
    PageResult<MesDvMaintenRecordLineDO> getMaintenRecordLinePage(MesDvMaintenRecordLinePageReqVO pageReqVO);

    /**
     * 获得指定保养记录的明细列表
     *
     * @param recordId 保养记录编号
     * @return 明细列表
     */
    List<MesDvMaintenRecordLineDO> getMaintenRecordLineListByRecordId(Long recordId);

    /**
     * 根据保养记录编号删除所有明细
     *
     * @param recordId 保养记录编号
     */
    void deleteMaintenRecordLineByRecordId(Long recordId);

}