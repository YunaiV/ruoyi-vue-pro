package cn.iocoder.yudao.module.hrm.service.recruit.post;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.hrm.controller.admin.recruit.vo.post.HrmRecruitPostPageReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.recruit.vo.post.HrmRecruitPostSaveReqVO;
import cn.iocoder.yudao.module.hrm.controller.admin.recruit.vo.post.HrmRecruitPostStatusReqVO;
import cn.iocoder.yudao.module.hrm.dal.dataobject.recruit.post.HrmRecruitPostDO;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * 招聘职位 Service 接口
 *
 * @author 芋道源码
 */
public interface HrmRecruitPostService {

    /**
     * 创建招聘职位
     *
     * @param createReqVO 招聘职位信息
     * @return 招聘职位编号
     */
    Long createRecruitPost(HrmRecruitPostSaveReqVO createReqVO);

    /**
     * 更新招聘职位
     *
     * @param updateReqVO 招聘职位信息
     */
    void updateRecruitPost(HrmRecruitPostSaveReqVO updateReqVO);

    /**
     * 获得招聘职位
     *
     * @param id 招聘职位编号
     * @return 招聘职位
     */
    HrmRecruitPostDO getRecruitPost(Long id);

    /**
     * 校验招聘职位是否存在
     *
     * @param id 招聘职位编号
     * @return 招聘职位
     */
    HrmRecruitPostDO validateRecruitPostExists(Long id);

    /**
     * 获得招聘职位列表
     *
     * @param ids 招聘职位编号集合
     * @return 招聘职位列表
     */
    List<HrmRecruitPostDO> getRecruitPostList(Collection<Long> ids);

    /**
     * 获得招聘职位 Map
     *
     * @param ids 招聘职位编号集合
     * @return 招聘职位 Map
     */
    default Map<Long, HrmRecruitPostDO> getRecruitPostMap(Collection<Long> ids) {
        return convertMap(getRecruitPostList(ids), HrmRecruitPostDO::getId);
    }

    /**
     * 获得招聘职位分页列表
     *
     * @param pageReqVO 分页查询
     * @return 招聘职位分页列表
     */
    PageResult<HrmRecruitPostDO> getRecruitPostPage(HrmRecruitPostPageReqVO pageReqVO);

    /**
     * 获得招聘职位精简列表
     *
     * @return 招聘职位精简列表
     */
    List<HrmRecruitPostDO> getRecruitPostSimpleList();

    /**
     * 更新招聘职位状态
     *
     * @param reqVO 招聘职位状态信息
     */
    void updateRecruitPostStatus(HrmRecruitPostStatusReqVO reqVO);

    /**
     * 获得招聘职位状态统计列表
     *
     * @param pageReqVO 分页查询
     * @return 状态与数量的映射
     */
    Map<Integer, Long> getRecruitPostStatusCount(HrmRecruitPostPageReqVO pageReqVO);

}
