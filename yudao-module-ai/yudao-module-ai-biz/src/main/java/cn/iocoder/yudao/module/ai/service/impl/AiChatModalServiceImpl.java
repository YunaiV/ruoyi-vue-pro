package cn.iocoder.yudao.module.ai.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.ai.convert.ChatModalConvert;
import cn.iocoder.yudao.module.ai.dal.dataobject.AiChatModalDO;
import cn.iocoder.yudao.module.ai.mapper.AiChatModalMapper;
import cn.iocoder.yudao.module.ai.service.AiChatModalService;
import cn.iocoder.yudao.module.ai.vo.AiChatModalAddReq;
import cn.iocoder.yudao.module.ai.vo.AiChatModalListReq;
import cn.iocoder.yudao.module.ai.vo.AiChatModalListRes;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * ai 模型
 *
 * @author fansili
 * @time 2024/4/24 19:42
 * @since 1.0
 */
@AllArgsConstructor
@Service
@Slf4j
public class AiChatModalServiceImpl implements AiChatModalService {

    private final AiChatModalMapper aiChatModalMapper;

    @Override
    public PageResult<AiChatModalListRes> list(AiChatModalListReq req) {
        LambdaQueryWrapperX<AiChatModalDO> queryWrapperX = new LambdaQueryWrapperX<>();
        // search
        if (!StrUtil.isBlank(req.getSearch())) {
            queryWrapperX.like(AiChatModalDO::getModelName, req.getSearch().trim());
        }
        // 默认排序
        queryWrapperX.orderByDesc(AiChatModalDO::getId);
        // 查询
        PageResult<AiChatModalDO> aiChatModalDOPageResult = aiChatModalMapper.selectPage(req, queryWrapperX);
        // 转换 res
        List<AiChatModalListRes> resList = ChatModalConvert.INSTANCE.convertAiChatModalListRes(aiChatModalDOPageResult.getList());
        return new PageResult<>(resList, aiChatModalDOPageResult.getTotal());
    }

    @Override
    public void add(AiChatModalAddReq req) {

    }
}
