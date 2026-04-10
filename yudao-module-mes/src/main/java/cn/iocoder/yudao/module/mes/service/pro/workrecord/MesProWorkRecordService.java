package cn.iocoder.yudao.module.mes.service.pro.workrecord;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.mes.controller.admin.pro.workrecord.vo.MesProWorkRecordLogPageReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.pro.workrecord.MesProWorkRecordDO;
import cn.iocoder.yudao.module.mes.dal.dataobject.pro.workrecord.MesProWorkRecordLogDO;

/**
 * MES 工作记录 Service 接口
 *
 * @author 芋道源码
 */
public interface MesProWorkRecordService {

    /**
     * 获得上下工记录流水分页
     *
     * @param pageReqVO 分页查询
     * @return 上下工记录流水分页
     */
    PageResult<MesProWorkRecordLogDO> getWorkRecordLogPage(MesProWorkRecordLogPageReqVO pageReqVO);

    /**
     * 获得上下工记录流水
     *
     * @param id 编号
     * @return 上下工记录流水
     */
    MesProWorkRecordLogDO getWorkRecordLog(Long id);

    /**
     * 上线（绑定工作站）
     *
     * @param userId        用户编号
     * @param workstationId 工作站编号
     * @return 记录编号
     */
    Long clockInWorkRecord(Long userId, Long workstationId);

    /**
     * 下线（解绑工作站）
     *
     * @param userId 用户编号
     * @return 记录编号
     */
    Long clockOutWorkRecord(Long userId);

    /**
     * 获取指定用户当前的绑定状态
     *
     * @param userId 用户编号
     * @return 绑定关系，如果未绑定则返回 null
     */
    MesProWorkRecordDO getWorkRecord(Long userId);

}
