package cn.iocoder.yudao.module.ai.convert;

import cn.iocoder.yudao.module.ai.controller.admin.image.vo.AiImageDallDrawingReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.image.vo.AiImageDallDrawingRespVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * ai image convert
 *
 * @author fansili
 * @time 2024/4/18 16:39
 * @since 1.0
 */
@Mapper
public interface AiImageConvert {

    AiImageConvert INSTANCE = Mappers.getMapper(AiImageConvert.class);

    /**
     * 转换 - AiImageDallDrawingRespVO
     *
     * @param req
     * @return
     */
    AiImageDallDrawingRespVO convertAiImageDallDrawingRespVO(AiImageDallDrawingReqVO req);
}
