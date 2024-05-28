package cn.iocoder.yudao.module.ai.convert;

import org.springframework.ai.models.midjourney.MidjourneyMessage;
import cn.iocoder.yudao.module.ai.controller.admin.image.vo.AiImageDallReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.image.vo.AiImageDallRespVO;
import cn.iocoder.yudao.module.ai.controller.admin.image.vo.AiImageListRespVO;
import cn.iocoder.yudao.module.ai.controller.admin.image.vo.AiImageMidjourneyOperationsVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.image.AiImageDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

// TODO @fan：convert 可以考虑去掉，使用 BeanUtils.copy 替代
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
    AiImageDallRespVO convertAiImageDallDrawingRespVO(AiImageDO req);

    /**
     * 转换 - AiImageDallDrawingRespVO
     *
     * @param req
     * @return
     */
    AiImageDallRespVO convertAiImageDallDrawingRespVO(AiImageDallReqVO req);

    /**
     * 转换 - AiImageListRespVO
     *
     * @param list
     * @return
     */
    List<AiImageListRespVO> convertAiImageListRespVO(List<AiImageDO> list);

    /**
     * 转换 - AiImageListRespVO
     *
     * @param aiImageDO
     * @return
     */
    AiImageListRespVO convertAiImageListRespVO(AiImageDO aiImageDO);

    /**
     * 转换 - AiImageMidjourneyOperationsVO
     *
     * @param component
     * @return
     */
    AiImageMidjourneyOperationsVO convertAiImageMidjourneyOperationsVO(MidjourneyMessage.Component component);

    /**
     * 转换 - AiImageDO
     *
     * @param req
     * @return
     */
    AiImageDO convertAiImageDO(AiImageDallReqVO req);
}
