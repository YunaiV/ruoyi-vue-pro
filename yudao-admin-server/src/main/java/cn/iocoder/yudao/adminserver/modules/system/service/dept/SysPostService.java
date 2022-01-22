package cn.iocoder.yudao.adminserver.modules.system.service.dept;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.adminserver.modules.system.controller.dept.vo.post.SysPostCreateReqVO;
import cn.iocoder.yudao.adminserver.modules.system.controller.dept.vo.post.SysPostExportReqVO;
import cn.iocoder.yudao.adminserver.modules.system.controller.dept.vo.post.SysPostPageReqVO;
import cn.iocoder.yudao.adminserver.modules.system.controller.dept.vo.post.SysPostUpdateReqVO;
import cn.iocoder.yudao.adminserver.modules.system.dal.dataobject.dept.SysPostDO;
import org.springframework.lang.Nullable;

import java.util.Collection;
import java.util.List;

import static cn.iocoder.yudao.framework.common.util.collection.SetUtils.asSet;

/**
 * 岗位 Service 接口
 *
 * @author 芋道源码
 */
public interface SysPostService {

    /**
     * 创建岗位
     *
     * @param reqVO 岗位信息
     * @return 岗位编号
     */
    Long createPost(SysPostCreateReqVO reqVO);

    /**
     * 更新岗位
     *
     * @param reqVO 岗位信息
     */
    void updatePost(SysPostUpdateReqVO reqVO);

    /**
     * 删除岗位信息
     *
     * @param id 岗位编号
     */
    void deletePost(Long id);

    /**
     * 获得岗位列表
     *
     * @param ids 岗位编号数组。如果为空，不进行筛选
     * @return 部门列表
     */
    default List<SysPostDO> getPosts(@Nullable Collection<Long> ids) {
        return getPosts(ids, asSet(CommonStatusEnum.ENABLE.getStatus(), CommonStatusEnum.DISABLE.getStatus()));
    }

    /**
     * 获得符合条件的岗位列表
     *
     * @param ids 岗位编号数组。如果为空，不进行筛选
     * @param statuses 状态数组。如果为空，不进行筛选
     * @return 部门列表
     */
    List<SysPostDO> getPosts(@Nullable Collection<Long> ids, @Nullable Collection<Integer> statuses);

    /**
     * 获得岗位分页列表
     *
     * @param reqVO 分页条件
     * @return 部门分页列表
     */
    PageResult<SysPostDO> getPostPage(SysPostPageReqVO reqVO);

    /**
     * 获得岗位列表
     *
     * @param reqVO 查询条件
     * @return 部门列表
     */
    List<SysPostDO> getPosts(SysPostExportReqVO reqVO);

    /**
     * 获得岗位信息
     *
     * @param id 岗位编号
     * @return 岗位信息
     */
    SysPostDO getPost(Long id);

    /**
     * 校验岗位们是否有效。如下情况，视为无效：
     * 1. 岗位编号不存在
     * 2. 岗位被禁用
     *
     * @param ids 岗位编号数组
     */
    void validPosts(Collection<Long> ids);

}
