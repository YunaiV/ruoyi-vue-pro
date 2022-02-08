package cn.iocoder.yudao.module.bpm.service.definition;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.model.BpmModelPageItemRespVO;
import cn.iocoder.yudao.module.bpm.controller.admin.definition.vo.model.BpmModelPageReqVO;

/**
 * 流程模型通用接口
 * Activiti 和 flowable 通用的流程模型接口
 *
 * @author yunlongn
 * @author jason
 */
public interface BpmModelCommonService {
    /**
     * 获得流程模型分页
     *
     * @param pageVO 分页查询
     * @return 流程模型分页
     */
    PageResult<BpmModelPageItemRespVO> getModelPage(BpmModelPageReqVO pageVO);
}
