package cn.iocoder.yudao.module.ai.dal.mysql.asr;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.ai.controller.admin.asr.vo.AiAsrPageReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.asr.AiAsrDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * AI 语音识别 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface AiAsrMapper extends BaseMapperX<AiAsrDO> {

    default PageResult<AiAsrDO> selectPage(AiAsrPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<AiAsrDO>()
                .eqIfPresent(AiAsrDO::getUserId, reqVO.getUserId())
                .eqIfPresent(AiAsrDO::getPlatform, reqVO.getPlatform())
                .eqIfPresent(AiAsrDO::getStatus, reqVO.getStatus())
                .likeIfPresent(AiAsrDO::getFileName, reqVO.getFileName())
                .betweenIfPresent(AiAsrDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(AiAsrDO::getId));
    }

    default PageResult<AiAsrDO> selectPageByUserId(Long userId, PageParam reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<AiAsrDO>()
                .eq(AiAsrDO::getUserId, userId)
                .orderByDesc(AiAsrDO::getId));
    }

}
