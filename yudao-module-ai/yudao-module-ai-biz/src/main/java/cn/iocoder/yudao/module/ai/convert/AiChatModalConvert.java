package cn.iocoder.yudao.module.ai.convert;

import cn.iocoder.yudao.module.ai.controller.admin.model.vo.model.AiChatModalAddReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.model.vo.model.AiChatModalListRespVO;
import cn.iocoder.yudao.module.ai.controller.admin.model.vo.model.AiChatModalRespVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiChatModalDO;
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
public interface AiChatModalConvert {

    AiChatModalConvert INSTANCE = Mappers.getMapper(AiChatModalConvert.class);

    /**
     * 转换 - AiChatModalListRes
     *
     * @param list
     * @return
     */
    List<AiChatModalListRespVO> convertAiChatModalListRes(List<AiChatModalDO> list);

    /**
     * 转换 - AiChatModalDO
     *
     * @param req
     * @return
     */
    AiChatModalDO convertAiChatModalDO(AiChatModalAddReqVO req);


    /**
     * 转换 - AiChatModalRes
     *
     * @param aiChatModalDO
     * @return
     */
    AiChatModalRespVO convertAiChatModalRes(AiChatModalDO aiChatModalDO);

}
