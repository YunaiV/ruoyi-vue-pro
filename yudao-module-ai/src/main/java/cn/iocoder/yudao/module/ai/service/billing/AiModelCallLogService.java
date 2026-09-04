package cn.iocoder.yudao.module.ai.service.billing;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.ai.controller.admin.model.vo.calllog.AiModelCallLogPageReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.model.vo.calllog.AiModelCallLogStatReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.model.vo.calllog.AiModelCallLogStatRespVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.billing.AiModelCallLogDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * AI 模型调用日志 Service 接口
 *
 * @author 芋道源码
 */
public interface AiModelCallLogService {

    /**
     * 创建调用日志（含自动计费）
     *
     * @param callLog 调用日志（不含费用字段，由本方法自动填充）
     * @return 编号
     */
    Long createCallLog(AiModelCallLogDO callLog);

    /**
     * 获得调用日志
     *
     * @param id 编号
     * @return 调用日志
     */
    AiModelCallLogDO getCallLog(Long id);

    /**
     * 获得调用日志分页
     *
     * @param pageReqVO 分页查询
     * @return 调用日志分页
     */
    PageResult<AiModelCallLogDO> getCallLogPage(@Valid AiModelCallLogPageReqVO pageReqVO);

    /**
     * 获得调用日志列表（用于导出）
     *
     * @param pageReqVO 查询条件
     * @return 调用日志列表
     */
    List<AiModelCallLogDO> getCallLogList(AiModelCallLogPageReqVO pageReqVO);

    /**
     * 获得调用日志统计
     *
     * @param statReqVO 统计条件
     * @return 统计结果
     */
    AiModelCallLogStatRespVO getCallLogStat(AiModelCallLogStatReqVO statReqVO);

}
