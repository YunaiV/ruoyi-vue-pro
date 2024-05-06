package cn.iocoder.yudao.module.ai.convert;

import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiChatModalDO;
import cn.iocoder.yudao.module.ai.controller.admin.model.vo.model.AiChatModalAddReq;
import cn.iocoder.yudao.module.ai.controller.admin.model.vo.model.AiChatModalListRes;
import cn.iocoder.yudao.module.ai.controller.admin.model.vo.model.AiChatModalRes;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
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
    List<AiChatModalListRes> convertAiChatModalListRes(List<AiChatModalDO> list);

    /**
     * 转换 - AiChatModalDO
     *
     * @param req
     * @return
     */
    @Mappings({
            @Mapping(target = "config", ignore = true)
    })
    AiChatModalDO convertAiChatModalDO(AiChatModalAddReq req);


    /**
     * 转换 - AiChatModalRes
     *
     * @param aiChatModalDO
     * @return
     */
    AiChatModalRes convertAiChatModalRes(AiChatModalDO aiChatModalDO);

}
