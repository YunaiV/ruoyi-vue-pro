package cn.iocoder.yudao.module.ai.convert;

import org.springframework.ai.models.midjourney.MidjourneyMessage;
import cn.iocoder.yudao.module.ai.controller.admin.image.vo.AiImageDallDrawingReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.image.vo.AiImageDallDrawingRespVO;
import cn.iocoder.yudao.module.ai.controller.admin.image.vo.AiImageListRespVO;
import cn.iocoder.yudao.module.ai.controller.admin.image.vo.AiImageMidjourneyOperationsVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.image.AiImageDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

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

    /**
     * 转换 - AiImageListRespVO
     *
     * @param list
     * @return
     */
    List<AiImageListRespVO> convertAiImageListRespVO(List<AiImageDO> list);

    /**
     * 转换 - AiImageMidjourneyOperationsVO
     *
     * @param component
     * @return
     */
    AiImageMidjourneyOperationsVO convertAiImageMidjourneyOperationsVO(MidjourneyMessage.Component component);
}
