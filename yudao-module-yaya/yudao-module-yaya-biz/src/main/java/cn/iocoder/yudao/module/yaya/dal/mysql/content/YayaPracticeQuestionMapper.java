package cn.iocoder.yudao.module.yaya.dal.mysql.content;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.yaya.dal.dataobject.content.YayaPracticeQuestionDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface YayaPracticeQuestionMapper extends BaseMapperX<YayaPracticeQuestionDO> {

    default List<YayaPracticeQuestionDO> selectListByTopicId(Long topicId) {
        return selectList(new LambdaQueryWrapperX<YayaPracticeQuestionDO>()
                .eq(YayaPracticeQuestionDO::getTopicId, topicId)
                .orderByAsc(YayaPracticeQuestionDO::getDisplayOrder)
                .orderByAsc(YayaPracticeQuestionDO::getId));
    }

    default int deleteByTopicId(Long topicId) {
        return delete(YayaPracticeQuestionDO::getTopicId, topicId);
    }

}
