package cn.iocoder.yudao.module.ai.service.write;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.ai.controller.admin.write.vo.AiWriteGenerateReqVO;
import reactor.core.publisher.Flux;

/**
 * AI 写作 Service 接口
 *
 * @author xiaoxin
 */
public interface AiWriteService {


    Flux<CommonResult<String>> generateComposition(AiWriteGenerateReqVO generateReqVO);


}
