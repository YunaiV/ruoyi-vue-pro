package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.SysPost;

/**
 * 岗位信息 服务层
 * 
 * @author ruoyi
 */
public interface ISysPostService
{
    /**
     * 查询岗位信息集合
     * 
     * @param post 岗位信息
     * @return 岗位列表
     */
    public List<SysPost> selectPostList(SysPost post);

    /**
     * 查询所有岗位
     * 
     * @return 岗位列表
     */
    public List<SysPost> selectPostAll();

    /**
     * 通过岗位ID查询岗位信息
     * 
     * @param postId 岗位ID
     * @return 角色对象信息
     */
    public SysPost selectPostById(Long postId);

    /**
     * 根据用户ID获取岗位选择框列表
     * 
     * @param userId 用户ID
     * @return 选中岗位ID列表
     */
    public List<Integer> selectPostListByUserId(Long userId);

    /**
     * 校验岗位名称
     * 
     * @param post 岗位信息
     * @return 结果
     */
    public String checkPostNameUnique(SysPost post);

    /**
     * 校验岗位编码
     * 
     * @param post 岗位信息
     * @return 结果
     */
    public String checkPostCodeUnique(SysPost post);

    /**
     * 通过岗位ID查询岗位使用数量
     * 
     * @param postId 岗位ID
     * @return 结果
     */
    public int countUserPostById(Long postId);

    /**
     * 删除岗位信息
     * 
     * @param postId 岗位ID
     * @return 结果
     */
    public int deletePostById(Long postId);

    /**
     * 批量删除岗位信息
     * 
     * @param postIds 需要删除的岗位ID
     * @return 结果
     * @throws Exception 异常
     */
    public int deletePostByIds(Long[] postIds);

    /**
     * 新增保存岗位信息
     * 
     * @param post 岗位信息
     * @return 结果
     */
    public int insertPost(SysPost post);

    /**
     * 修改保存岗位信息
     * 
     * @param post 岗位信息
     * @return 结果
     */
    public int updatePost(SysPost post);
}
