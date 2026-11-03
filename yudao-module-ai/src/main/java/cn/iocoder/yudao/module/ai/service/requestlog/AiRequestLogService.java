package cn.iocoder.yudao.module.ai.service.requestlog;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.ai.controller.admin.requestlog.vo.AiRequestLogPageReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.requestlog.vo.AiRequestLogStatisticsRespVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiModelDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.requestlog.AiRequestLogDO;
import cn.iocoder.yudao.module.ai.enums.requestlog.AiRequestLogBizTypeEnum;
import cn.iocoder.yudao.module.ai.service.requestlog.dto.AiRequestLogCreateReqDTO;
import org.springframework.ai.chat.model.ChatResponse;

/**
 * AI 请求日志 Service 接口
 *
 * @author 芋道源码
 */
public interface AiRequestLogService {

    /**
     * 构建请求日志的创建快照，在调用模型前执行
     *
     * 捕获当前租户、开始时间，以及调用时的平台、模型快照
     *
     * @param requestId 请求编号，服务端生成的本地 UUID
     * @param userId    用户编号
     * @param bizType   业务类型
     * @param bizId     业务编号
     * @param model     模型
     * @return 请求日志的创建快照
     */
    AiRequestLogCreateReqDTO buildCreateReqDTO(String requestId, Long userId, AiRequestLogBizTypeEnum bizType, Long bizId, AiModelDO model);

    // DONE @AI：应该叫 setCreateReqDTO 把；更好理解吧。（已由原方法名 setCreateReqDTOResponse 改名，调用方同步）
    /**
     * 根据模型响应（同步结果或流式 chunk），设置创建快照的用量与响应元数据；只修改内存中的快照，不写数据库
     *
     * null / EmptyUsage 不覆盖已有用量；有效用量整体替换，不逐块累加；解析失败不影响生成。
     * 请求日志创建后，不再设置
     *
     * @param createReqDTO 请求日志的创建快照
     * @param response     模型响应
     */
    void setCreateReqDTO(AiRequestLogCreateReqDTO createReqDTO, ChatResponse response);

    /**
     * 创建请求日志
     *
     * 在独立事务中写入，业务事务回滚时日志仍保留；仅供内部计量使用，不对外开放 HTTP 接口
     *
     * @param createReqDTO 请求日志的创建快照，调用方需设置状态、错误信息
     */
    void createRequestLog(AiRequestLogCreateReqDTO createReqDTO);

    /**
     * 获得请求日志
     *
     * @param id 编号
     * @return 请求日志
     */
    AiRequestLogDO getRequestLog(Long id);

    /**
     * 获得请求日志分页
     *
     * @param pageReqVO 分页查询
     * @return 请求日志分页
     */
    PageResult<AiRequestLogDO> getRequestLogPage(AiRequestLogPageReqVO pageReqVO);

    /**
     * 获得请求日志统计
     *
     * 统计所有匹配筛选条件的记录，不受分页参数影响
     *
     * @param pageReqVO 筛选条件，与分页查询一致
     * @return 请求日志统计
     */
    AiRequestLogStatisticsRespVO getRequestLogStatistics(AiRequestLogPageReqVO pageReqVO);

}
