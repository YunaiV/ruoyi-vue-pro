package cn.iocoder.yudao.module.oa.dal.mysql.note;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.oa.dal.dataobject.note.OaNoteReceiverDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

/**
 * OA 笔记接收人 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface OaNoteReceiverMapper extends BaseMapperX<OaNoteReceiverDO> {

    default void deleteByNoteIdAndUserIds(Long noteId, Collection<Long> userIds) {
        delete(new LambdaQueryWrapperX<OaNoteReceiverDO>()
                .eq(OaNoteReceiverDO::getNoteId, noteId)
                .in(OaNoteReceiverDO::getUserId, userIds));
    }

    default List<OaNoteReceiverDO> selectListByNoteId(Long noteId) {
        return selectList(OaNoteReceiverDO::getNoteId, noteId);
    }

    default OaNoteReceiverDO selectByNoteIdAndUserId(Long noteId, Long userId) {
        return selectOne(OaNoteReceiverDO::getNoteId, noteId, OaNoteReceiverDO::getUserId, userId);
    }

    default List<OaNoteReceiverDO> selectListByUserId(Long userId) {
        return selectList(OaNoteReceiverDO::getUserId, userId);
    }

    default List<OaNoteReceiverDO> selectListByNoteIds(Collection<Long> noteIds) {
        return selectList(new LambdaQueryWrapperX<OaNoteReceiverDO>().in(OaNoteReceiverDO::getNoteId, noteIds));
    }

    default void deleteByNoteId(Long noteId) {
        delete(OaNoteReceiverDO::getNoteId, noteId);
    }

    default void deleteByNoteIds(Collection<Long> noteIds) {
        delete(new LambdaQueryWrapperX<OaNoteReceiverDO>().in(OaNoteReceiverDO::getNoteId, noteIds));
    }

}
