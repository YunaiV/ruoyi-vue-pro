package cn.iocoder.yudao.module.ai.convert;

import cn.iocoder.yudao.module.ai.controller.admin.model.vo.model.AiChatModelAddReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.model.vo.model.AiChatModelListRespVO;
import cn.iocoder.yudao.module.ai.controller.admin.model.vo.model.AiChatModalRespVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiChatModelDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 聊天 modal
 *
 * @author fansili
 * @time 2024/4/18 16:39
 * @since 1.0
 */
@Mapper
public interface AiChatModelConvert {

    AiChatModelConvert INSTANCE = Mappers.getMapper(AiChatModelConvert.class);

    /**
     * 转换 - AiChatModalListRes
     *
     * @param list
     * @return
     */
    List<AiChatModelListRespVO> convertAiChatModalListRes(List<AiChatModelDO> list);

    /**
     * 转换 - AiChatModalDO
     *
     * @param req
     * @return
     */
    AiChatModelDO convertAiChatModalDO(AiChatModelAddReqVO req);


    /**
     * 转换 - AiChatModalRes
     *
     * @param aiChatModalDO
     * @return
     */
    AiChatModalRespVO convertAiChatModalRes(AiChatModelDO aiChatModalDO);

}
