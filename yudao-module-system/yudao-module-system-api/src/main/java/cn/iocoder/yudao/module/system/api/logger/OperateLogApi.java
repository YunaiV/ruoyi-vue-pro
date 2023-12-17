package cn.iocoder.yudao.module.system.api.logger;

import cn.iocoder.yudao.module.system.api.logger.dto.OperateLogCreateReqDTO;
import cn.iocoder.yudao.module.system.api.logger.dto.OperateLogV2RespDTO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * 操作日志 API 接口
 *
 * @author 芋道源码
 */
public interface OperateLogApi {

    /**
     * 创建操作日志
     *
     * @param createReqDTO 请求
     */
    void createOperateLog(@Valid OperateLogCreateReqDTO createReqDTO);

    /**
     * 获取指定模块的指定数据的操作日志
     *
     * @param module 操作模块
     * @param bizId  操作模块编号
     * @return 操作日志
     */
    List<OperateLogV2RespDTO> getOperateLogByModuleAndBizId(String module, Long bizId);

}
