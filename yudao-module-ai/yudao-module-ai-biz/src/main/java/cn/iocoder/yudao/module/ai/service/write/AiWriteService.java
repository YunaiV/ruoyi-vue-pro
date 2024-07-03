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


    /**
     * 生成写作内容
     *
     * @param generateReqVO 作文生成请求参数
     * @param userId        用户编号
     * @return 生成结果
     */
    Flux<CommonResult<String>> generateWriteContent(AiWriteGenerateReqVO generateReqVO, Long userId);


}
