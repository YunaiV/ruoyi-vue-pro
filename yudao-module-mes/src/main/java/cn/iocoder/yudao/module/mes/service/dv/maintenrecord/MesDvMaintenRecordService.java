package cn.iocoder.yudao.module.mes.service.dv.maintenrecord;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.mes.controller.admin.dv.maintenrecord.vo.MesDvMaintenRecordPageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.dv.maintenrecord.vo.MesDvMaintenRecordSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.dv.maintenrecord.MesDvMaintenRecordDO;

import jakarta.validation.Valid;

/**
 * 设备保养记录 Service 接口
 *
 * @author 芋道源码
 */
public interface MesDvMaintenRecordService {

    /**
     * 创建设备保养记录
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createMaintenRecord(@Valid MesDvMaintenRecordSaveReqVO createReqVO);

    /**
     * 更新设备保养记录
     *
     * @param updateReqVO 更新信息
     */
    void updateMaintenRecord(@Valid MesDvMaintenRecordSaveReqVO updateReqVO);

    /**
     * 提交设备保养记录（草稿→已提交）
     *
     * @param id 编号
     */
    void submitMaintenRecord(Long id);

    /**
     * 删除设备保养记录
     *
     * @param id 编号
     */
    void deleteMaintenRecord(Long id);

    /**
     * 校验设备保养记录存在
     *
     * @param id 编号
     */
    void validateMaintenRecordExists(Long id);

    /**
     * 校验设备保养记录是否为草稿状态
     *
     * @param id 编号
     * @return 保养记录
     */
    MesDvMaintenRecordDO validateMaintenRecordDraft(Long id);

    /**
     * 获得指定设备的保养记录数量
     *
     * @param machineryId 设备编号
     * @return 保养记录数量
     */
    Long getMaintenRecordCountByMachineryId(Long machineryId);

    /**
     * 获得设备保养记录
     *
     * @param id 编号
     * @return 设备保养记录
     */
    MesDvMaintenRecordDO getMaintenRecord(Long id);

    /**
     * 获得设备保养记录分页
     *
     * @param pageReqVO 分页查询
     * @return 设备保养记录分页
     */
    PageResult<MesDvMaintenRecordDO> getMaintenRecordPage(MesDvMaintenRecordPageReqVO pageReqVO);

}
