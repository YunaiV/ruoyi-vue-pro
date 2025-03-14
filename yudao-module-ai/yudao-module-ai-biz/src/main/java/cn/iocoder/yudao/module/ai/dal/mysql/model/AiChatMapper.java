package cn.iocoder.yudao.module.ai.dal.mysql.model;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.QueryWrapperX;
import cn.iocoder.yudao.module.ai.controller.admin.model.vo.model.AiModelPageReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.model.AiModelDO;
import org.apache.ibatis.annotations.Mapper;

import javax.annotation.Nullable;
import java.util.List;

/**
 * API 模型 Mapper
 *
 * @author fansili
 */
@Mapper
public interface AiChatMapper extends BaseMapperX<AiModelDO> {

    default AiModelDO selectFirstByStatus(Integer type, Integer status) {
        return selectOne(new QueryWrapperX<AiModelDO>()
                .eq("type", type)
                .eq("status", status)
                .limitN(1)
                .orderByAsc("sort"));
    }

    default PageResult<AiModelDO> selectPage(AiModelPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<AiModelDO>()
                .likeIfPresent(AiModelDO::getName, reqVO.getName())
                .eqIfPresent(AiModelDO::getModel, reqVO.getModel())
                .eqIfPresent(AiModelDO::getPlatform, reqVO.getPlatform())
                .orderByAsc(AiModelDO::getSort));
    }

    default List<AiModelDO> selectListByStatusAndType(Integer status, Integer type,
                                                      @Nullable String platform) {
        return selectList(new LambdaQueryWrapperX<AiModelDO>()
                .eq(AiModelDO::getStatus, status)
                .eq(AiModelDO::getType, type)
                .eqIfPresent(AiModelDO::getPlatform, platform)
                .orderByAsc(AiModelDO::getSort));
    }

}
