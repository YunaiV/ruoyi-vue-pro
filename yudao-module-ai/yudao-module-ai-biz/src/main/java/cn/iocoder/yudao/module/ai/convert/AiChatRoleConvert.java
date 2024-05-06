package cn.iocoder.yudao.module.ai.convert;

import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiChatRoleDO;
import cn.iocoder.yudao.module.ai.controller.admin.model.vo.role.AiChatRoleAddReq;
import cn.iocoder.yudao.module.ai.controller.admin.model.vo.role.AiChatRoleRes;
import cn.iocoder.yudao.module.ai.controller.admin.model.vo.role.AiChatRoleUpdateReq;
import cn.iocoder.yudao.module.ai.controller.admin.model.vo.role.AiChatRoleListRes;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 聊天 对话 convert
 *
 * @author fansili
 * @time 2024/4/18 16:39
 * @since 1.0
 */
@Mapper
public interface AiChatRoleConvert {

    AiChatRoleConvert INSTANCE = Mappers.getMapper(AiChatRoleConvert.class);

    /**
     * 转换 - ChatRoleListRes
     *
     * @param roleList
     * @return
     */
    List<AiChatRoleListRes> convertChatRoleListRes(List<AiChatRoleDO> roleList);

    /**
     * 转换 - AiChatRoleDO
     *
     * @param req
     * @return
     */
    AiChatRoleDO convertAiChatRoleDO(AiChatRoleAddReq req);

    /**
     * 转换 - AiChatRoleDO
     *
     * @param req
     * @return
     */
    AiChatRoleDO convertAiChatRoleDO(AiChatRoleUpdateReq req);

    /**
     * 转换 - AiChatRoleRes
     *
     * @param aiChatRoleDO
     * @return
     */
    AiChatRoleRes convertAiChatRoleRes(AiChatRoleDO aiChatRoleDO);
}
