package cn.iocoder.yudao.module.oa.service.discussion;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.oa.controller.admin.discussion.vo.discussion.OaDiscussionPageReqVO;
import cn.iocoder.yudao.module.oa.controller.admin.discussion.vo.discussion.OaDiscussionSaveReqVO;
import cn.iocoder.yudao.module.oa.dal.dataobject.discussion.OaDiscussionDO;

/**
 * OA 讨论 Service 接口
 *
 * @author 芋道源码
 */
public interface OaDiscussionService {

    /**
     * 创建讨论
     *
     * @param createReqVO 创建信息
     * @param userId 用户编号
     * @return 讨论编号
     */
    Long createDiscussion(OaDiscussionSaveReqVO createReqVO, Long userId);

    /**
     * 更新讨论
     *
     * @param updateReqVO 更新信息
     * @param userId 用户编号
     */
    void updateDiscussion(OaDiscussionSaveReqVO updateReqVO, Long userId);

    /**
     * 删除讨论
     *
     * @param id 讨论编号
     * @param userId 用户编号
     */
    void deleteDiscussion(Long id, Long userId);

    /**
     * 获得讨论
     *
     * @param id 讨论编号
     * @return 讨论
     */
    OaDiscussionDO getDiscussion(Long id);

    /**
     * 获得讨论分页
     *
     * @param pageReqVO 分页查询
     * @return 讨论分页
     */
    PageResult<OaDiscussionDO> getDiscussionPage(OaDiscussionPageReqVO pageReqVO);

    /**
     * 获得管理范围内的讨论分页
     *
     * @param pageReqVO 分页查询
     * @param userId 当前用户编号
     * @return 管理员可查询全部讨论，普通用户仅查询本人讨论
     */
    PageResult<OaDiscussionDO> getDiscussionManagePage(OaDiscussionPageReqVO pageReqVO, Long userId);

    /**
     * 增加讨论访问次数
     *
     * @param id 讨论编号
     */
    void increaseDiscussionVisitCount(Long id);

    /**
     * 校验讨论存在
     *
     * @param id 讨论编号
     * @return 讨论，不存在时抛出异常
     */
    OaDiscussionDO validateDiscussionExists(Long id);

}
