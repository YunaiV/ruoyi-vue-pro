package cn.iocoder.yudao.module.ai.dal.mysql.model;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.ai.controller.admin.model.vo.tool.AiToolPageReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiToolDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * AI 工具 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface AiToolMapper extends BaseMapperX<AiToolDO> {

    default PageResult<AiToolDO> selectPage(AiToolPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<AiToolDO>()
                .likeIfPresent(AiToolDO::getName, reqVO.getName())
                .eqIfPresent(AiToolDO::getDescription, reqVO.getDescription())
                .eqIfPresent(AiToolDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(AiToolDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(AiToolDO::getId));
    }

    default List<AiToolDO> selectListByStatus(Integer status) {
        return selectList(new LambdaQueryWrapperX<AiToolDO>()
                .eq(AiToolDO::getStatus, status)
                .orderByDesc(AiToolDO::getId));
    }

}