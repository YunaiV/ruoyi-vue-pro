package cn.iocoder.yudao.adminserver.modules.system.service.logger;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.adminserver.modules.system.controller.logger.vo.loginlog.SysLoginLogCreateReqVO;
import cn.iocoder.yudao.adminserver.modules.system.controller.logger.vo.loginlog.SysLoginLogExportReqVO;
import cn.iocoder.yudao.adminserver.modules.system.controller.logger.vo.loginlog.SysLoginLogPageReqVO;
import cn.iocoder.yudao.adminserver.modules.system.dal.dataobject.logger.SysLoginLogDO;

import java.util.List;

/**
 * 登陆日志 Service 接口
 */
public interface SysLoginLogService {

    /**
     * 创建登陆日志
     *
     * @param reqVO 日志信息
     */
    void createLoginLog(SysLoginLogCreateReqVO reqVO);

    /**
     * 获得登陆日志分页
     *
     * @param reqVO 分页条件
     * @return 登陆日志分页
     */
    PageResult<SysLoginLogDO> getLoginLogPage(SysLoginLogPageReqVO reqVO);

    /**
     * 获得登陆日志列表
     *
     * @param reqVO 列表条件
     * @return 登陆日志列表
     */
    List<SysLoginLogDO> getLoginLogList(SysLoginLogExportReqVO reqVO);

}
