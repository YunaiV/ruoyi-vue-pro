package cn.iocoder.yudao.module.system.service.logger;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.system.api.logger.dto.OperateLogCreateReqDTO;
import cn.iocoder.yudao.module.system.controller.admin.logger.vo.operatelog.OperateLogPageReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.logger.OperateLogDO;
import cn.iocoder.yudao.module.system.dal.dataobject.logger.OperateLogV2DO;
import cn.iocoder.yudao.module.system.service.logger.bo.OperateLogV2CreateReqBO;

import java.util.List;

/**
 * 操作日志 Service 接口
 *
 * @author 芋道源码
 */
public interface OperateLogService {

    /**
     * 记录操作日志
     *
     * @param createReqDTO 操作日志请求
     */
    void createOperateLog(OperateLogCreateReqDTO createReqDTO);

    /**
     * 获得操作日志分页列表
     *
     * @param pageReqVO 分页条件
     * @return 操作日志分页列表
     */
    PageResult<OperateLogDO> getOperateLogPage(OperateLogPageReqVO pageReqVO);

    // ======================= LOG V2 =======================

    /**
     * 记录操作日志 V2
     *
     * @param createReqBO 创建请求
     */
    void createOperateLogV2(OperateLogV2CreateReqBO createReqBO);

    // TODO @puhui999：module 改成 type
    /**
     * 获取指定模块的指定数据的操作日志
     *
     * @param module 操作模块
     * @param bizId  操作模块编号
     * @return 操作日志
     */
    List<OperateLogV2DO> getOperateLogByModuleAndBizId(String module, Long bizId);

}
