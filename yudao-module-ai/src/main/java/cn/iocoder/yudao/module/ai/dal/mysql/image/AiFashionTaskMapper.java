package cn.iocoder.yudao.module.ai.dal.mysql.image;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.ai.controller.admin.image.vo.fashion.AiFashionTaskPageReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.image.AiFashionTaskDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * AI 服装设计流水线主任务 Mapper
 *
 * @author deepay
 */
@Mapper
public interface AiFashionTaskMapper extends BaseMapperX<AiFashionTaskDO> {

    default PageResult<AiFashionTaskDO> selectPage(AiFashionTaskPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<AiFashionTaskDO>()
                .eqIfPresent(AiFashionTaskDO::getUserId, reqVO.getUserId())
                .eqIfPresent(AiFashionTaskDO::getStatus, reqVO.getStatus())
                .likeIfPresent(AiFashionTaskDO::getPrompt, reqVO.getPrompt())
                .betweenIfPresent(AiFashionTaskDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(AiFashionTaskDO::getId));
    }

}
