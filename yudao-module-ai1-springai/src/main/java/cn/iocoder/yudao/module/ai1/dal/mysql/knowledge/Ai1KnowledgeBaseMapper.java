package cn.iocoder.yudao.module.ai1.dal.mysql.knowledge;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.ai1.controller.admin.knowledge.vo.knowledgebase.Ai1KnowledgeBasePageReqVO;
import cn.iocoder.yudao.module.ai1.dal.dataobject.knowledge.Ai1KnowledgeBaseDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * AI1 知识库 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface Ai1KnowledgeBaseMapper extends BaseMapperX<Ai1KnowledgeBaseDO> {

    default PageResult<Ai1KnowledgeBaseDO> selectPage(Ai1KnowledgeBasePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<Ai1KnowledgeBaseDO>()
                .likeIfPresent(Ai1KnowledgeBaseDO::getName, reqVO.getName())
                .eqIfPresent(Ai1KnowledgeBaseDO::getStatus, reqVO.getStatus())
                .orderByAsc(Ai1KnowledgeBaseDO::getId));
    }

    default List<Ai1KnowledgeBaseDO> selectListByStatus(Integer status) {
        return selectList(new LambdaQueryWrapperX<Ai1KnowledgeBaseDO>()
                .eq(Ai1KnowledgeBaseDO::getStatus, status)
                .orderByAsc(Ai1KnowledgeBaseDO::getId));
    }

}
