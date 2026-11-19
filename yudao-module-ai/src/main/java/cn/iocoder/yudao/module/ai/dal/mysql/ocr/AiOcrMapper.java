package cn.iocoder.yudao.module.ai.dal.mysql.ocr;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.ai.controller.admin.ocr.vo.AiOcrPageReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.ocr.AiOcrDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * AI 文字识别 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface AiOcrMapper extends BaseMapperX<AiOcrDO> {

    default PageResult<AiOcrDO> selectPage(AiOcrPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<AiOcrDO>()
                .eqIfPresent(AiOcrDO::getUserId, reqVO.getUserId())
                .eqIfPresent(AiOcrDO::getPlatform, reqVO.getPlatform())
                .eqIfPresent(AiOcrDO::getStatus, reqVO.getStatus())
                .likeIfPresent(AiOcrDO::getFileName, reqVO.getFileName())
                .betweenIfPresent(AiOcrDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(AiOcrDO::getId));
    }

    default PageResult<AiOcrDO> selectPageByUserId(Long userId, PageParam reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<AiOcrDO>()
                .eq(AiOcrDO::getUserId, userId)
                .orderByDesc(AiOcrDO::getId));
    }

}
