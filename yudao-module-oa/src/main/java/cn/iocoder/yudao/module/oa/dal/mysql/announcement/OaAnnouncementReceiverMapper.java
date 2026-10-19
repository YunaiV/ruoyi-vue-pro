package cn.iocoder.yudao.module.oa.dal.mysql.announcement;

import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.oa.dal.dataobject.announcement.OaAnnouncementReceiverDO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

/**
 * OA 公告接收关系 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface OaAnnouncementReceiverMapper extends BaseMapperX<OaAnnouncementReceiverDO> {

    default OaAnnouncementReceiverDO selectByAnnouncementIdAndReceiverUserId(Long announcementId,
                                                                               Long receiverUserId) {
        return selectOne(OaAnnouncementReceiverDO::getAnnouncementId, announcementId,
                OaAnnouncementReceiverDO::getReceiverUserId, receiverUserId);
    }

    default List<OaAnnouncementReceiverDO> selectListByAnnouncementIds(Collection<Long> announcementIds) {
        return selectList(new LambdaQueryWrapperX<OaAnnouncementReceiverDO>()
                .in(OaAnnouncementReceiverDO::getAnnouncementId, announcementIds));
    }

    default List<OaAnnouncementReceiverDO> selectListByAnnouncementId(Long announcementId) {
        return selectList(OaAnnouncementReceiverDO::getAnnouncementId, announcementId);
    }

    @Delete("DELETE FROM oa_announcement_receiver WHERE announcement_id = #{announcementId}")
    void deleteByAnnouncementId(@Param("announcementId") Long announcementId);

    @Delete("DELETE FROM oa_announcement_receiver WHERE id = #{id}")
    void deleteByIdPhysical(@Param("id") Long id);

}
