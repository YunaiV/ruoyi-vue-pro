package cn.iocoder.yudao.module.oa.service.announcement;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.oa.controller.admin.announcement.vo.OaAnnouncementPageReqVO;
import cn.iocoder.yudao.module.oa.controller.admin.announcement.vo.OaAnnouncementSaveReqVO;
import cn.iocoder.yudao.module.oa.dal.dataobject.announcement.OaAnnouncementDO;
import cn.iocoder.yudao.module.oa.dal.dataobject.announcement.OaAnnouncementReceiverDO;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMultiMap;

/**
 * OA 公告 Service 接口
 *
 * @author 芋道源码
 */
public interface OaAnnouncementService {

    /**
     * 创建公告
     *
     * @param createReqVO 创建信息
     * @param userId 用户编号
     * @return 公告编号
     */
    Long createAnnouncement(OaAnnouncementSaveReqVO createReqVO, Long userId);

    /**
     * 更新公告
     *
     * @param updateReqVO 更新信息
     * @param userId 用户编号
     */
    void updateAnnouncement(OaAnnouncementSaveReqVO updateReqVO, Long userId);

    /**
     * 删除发布的公告
     *
     * @param id 公告编号
     * @param userId 用户编号
     */
    void deleteAnnouncement(Long id, Long userId);

    /**
     * 删除接收的公告
     *
     * @param id 公告编号
     * @param userId 用户编号
     */
    void deleteReceivedAnnouncement(Long id, Long userId);

    /**
     * 获得公告
     *
     * @param id 公告编号
     * @param userId 用户编号
     * @return 公告
     */
    OaAnnouncementDO getAnnouncement(Long id, Long userId);

    /**
     * 获得发布的公告分页
     *
     * @param pageReqVO 分页查询
     * @param userId 用户编号
     * @return 公告分页
     */
    PageResult<OaAnnouncementDO> getPublishedAnnouncementPage(OaAnnouncementPageReqVO pageReqVO, Long userId);

    /**
     * 获得接收的公告分页
     *
     * @param pageReqVO 分页查询
     * @param userId 用户编号
     * @return 公告分页
     */
    PageResult<OaAnnouncementDO> getReceivedAnnouncementPage(OaAnnouncementPageReqVO pageReqVO, Long userId);

    /**
     * 标记公告为已读
     *
     * @param id 公告编号
     * @param userId 用户编号
     */
    void updateAnnouncementReadStatus(Long id, Long userId);

    /**
     * 转发公告给当前用户的直属下属
     *
     * @param id 公告编号
     * @param userId 用户编号
     * @return 新增接收人数
     */
    int forwardAnnouncement(Long id, Long userId);

    /**
     * 获得公告接收关系 Map
     *
     * @param announcementIds 公告编号集合
     * @return 公告编号与接收关系列表的 Map
     */
    default Map<Long, List<OaAnnouncementReceiverDO>> getAnnouncementReceiverListMap(Collection<Long> announcementIds) {
        return convertMultiMap(getAnnouncementReceiverList(announcementIds), OaAnnouncementReceiverDO::getAnnouncementId);
    }

    /**
     * 获得公告接收关系列表
     *
     * @param announcementIds 公告编号集合
     * @return 公告接收关系列表
     */
    List<OaAnnouncementReceiverDO> getAnnouncementReceiverList(Collection<Long> announcementIds);

}
