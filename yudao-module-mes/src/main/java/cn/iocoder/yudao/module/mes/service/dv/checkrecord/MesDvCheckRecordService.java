package cn.iocoder.yudao.module.mes.service.dv.checkrecord;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.mes.controller.admin.dv.checkrecord.vo.MesDvCheckRecordPageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.dv.checkrecord.vo.MesDvCheckRecordSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.dv.checkrecord.MesDvCheckRecordDO;

import jakarta.validation.Valid;

/**
 * MES 设备点检记录 Service 接口
 *
 * @author 芋道源码
 */
public interface MesDvCheckRecordService {

    /**
     * 创建设备点检记录
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createCheckRecord(@Valid MesDvCheckRecordSaveReqVO createReqVO);

    /**
     * 更新设备点检记录
     *
     * @param updateReqVO 更新信息
     */
    void updateCheckRecord(@Valid MesDvCheckRecordSaveReqVO updateReqVO);

    /**
     * 提交点检记录（状态从草稿变为已完成）
     *
     * @param id 编号
     */
    void submitCheckRecord(Long id);

    /**
     * 删除设备点检记录
     *
     * @param id 编号
     */
    void deleteCheckRecord(Long id);

    /**
     * 校验设备点检记录存在
     *
     * @param id 编号
     */
    void validateCheckRecordExists(Long id);

    /**
     * 校验点检记录为草稿状态
     *
     * @param id 编号
     */
    void validateCheckRecordDraft(Long id);

    /**
     * 获得指定设备的点检记录数量
     *
     * @param machineryId 设备编号
     * @return 点检记录数量
     */
    Long getCheckRecordCountByMachineryId(Long machineryId);

    /**
     * 获得设备点检记录
     *
     * @param id 编号
     * @return 设备点检记录
     */
    MesDvCheckRecordDO getCheckRecord(Long id);

    /**
     * 获得设备点检记录分页
     *
     * @param pageReqVO 分页查询
     * @return 设备点检记录分页
     */
    PageResult<MesDvCheckRecordDO> getCheckRecordPage(MesDvCheckRecordPageReqVO pageReqVO);

}
