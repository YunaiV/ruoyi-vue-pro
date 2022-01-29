package cn.iocoder.yudao.module.system.service.sms;

import cn.iocoder.yudao.module.system.controller.sms.vo.log.SysSmsLogExportReqVO;
import cn.iocoder.yudao.module.system.controller.sms.vo.log.SysSmsLogPageReqVO;
import cn.iocoder.yudao.coreservice.modules.system.dal.dataobject.sms.SysSmsLogDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import java.util.List;

/**
 * 短信日志 Service 接口
 *
 * @author zzf
 * @date 13:48 2021/3/2
 */
public interface SysSmsLogService {

    /**
     * 获得短信日志分页
     *
     * @param pageReqVO 分页查询
     * @return 短信日志分页
     */
    PageResult<SysSmsLogDO> getSmsLogPage(SysSmsLogPageReqVO pageReqVO);

    /**
     * 获得短信日志列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 短信日志列表
     */
    List<SysSmsLogDO> getSmsLogList(SysSmsLogExportReqVO exportReqVO);

}
