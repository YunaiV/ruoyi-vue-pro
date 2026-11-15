package cn.iocoder.yudao.module.ai.service.drama;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.project.*;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.task.AiDramaTaskPageReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.drama.AiDramaProjectDO;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * AI 短剧项目 Service 接口
 *
 * @author 芋道源码
 */
public interface AiDramaProjectService {

    // ========== 我的项目 ==========

    /**
     * 创建项目
     *
     * @param userId 用户编号
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createProject(Long userId, AiDramaProjectSaveReqVO createReqVO);

    /**
     * 修改我的项目
     *
     * @param userId 用户编号
     * @param updateReqVO 修改信息
     */
    void updateProjectMy(Long userId, AiDramaProjectSaveReqVO updateReqVO);

    /**
     * 修改我的项目的当前步骤
     *
     * @param userId 用户编号
     * @param updateReqVO 修改信息
     */
    void updateProjectStatusMy(Long userId, AiDramaProjectUpdateStatusReqVO updateReqVO);

    /**
     * 删除我的项目：一并删除剧本、资产、分镜
     *
     * @param id 编号
     * @param userId 用户编号
     */
    void deleteProjectMy(Long id, Long userId);

    /**
     * 获得我的项目分页
     *
     * @param userId 用户编号
     * @param pageReqVO 分页查询
     * @return 分页
     */
    PageResult<AiDramaProjectDO> getProjectMyPage(Long userId, AiDramaProjectMyPageReqVO pageReqVO);

    /**
     * 校验项目存在，且属于该用户
     *
     * @param id 编号
     * @param userId 用户编号
     * @return 项目
     */
    AiDramaProjectDO validateProjectMy(Long id, Long userId);

    /**
     * 合成成片：异步执行，前端轮询项目的成片状态
     *
     * @param userId 用户编号
     * @param reqVO 合成参数
     */
    void composeProjectMy(Long userId, AiDramaComposeReqVO reqVO);

    // ========== 公开广场 ==========

    /**
     * 获得公开项目分页：已公开且成片已完成
     *
     * @param pageReqVO 分页查询
     * @return 分页
     */
    PageResult<AiDramaProjectDO> getProjectPublicPage(AiDramaProjectPublicPageReqVO pageReqVO);

    /**
     * 获得公开项目
     *
     * @param id 编号
     * @return 项目
     */
    AiDramaProjectDO getProjectPublic(Long id);

    // ========== 管理 ==========

    /**
     * 获得项目分页
     *
     * @param pageReqVO 分页查询
     * @return 分页
     */
    PageResult<AiDramaProjectDO> getProjectPage(AiDramaProjectPageReqVO pageReqVO);

    /**
     * 获得项目
     *
     * @param id 编号
     * @return 项目
     */
    AiDramaProjectDO getProject(Long id);

    /**
     * 校验项目存在
     *
     * @param id 编号
     * @return 项目
     */
    AiDramaProjectDO validateProjectExists(Long id);

    /**
     * 获得项目列表
     *
     * @param ids 编号
     * @return 项目列表
     */
    List<AiDramaProjectDO> getProjectList(Collection<Long> ids);

    /**
     * 获得用户的项目列表
     *
     * @param userId 用户编号
     * @return 项目列表
     */
    List<AiDramaProjectDO> getProjectListByUserId(Long userId);

    /**
     * 修改项目的公开状态
     *
     * @param updateReqVO 修改信息
     */
    void updateProjectPublicStatus(AiDramaProjectUpdatePublicReqVO updateReqVO);

    /**
     * 删除项目：一并删除剧本、资产、分镜
     *
     * @param id 编号
     */
    void deleteProject(Long id);

    /**
     * 重新合成成片：使用全部已完成的镜头，项目当前的画幅与转场；不含旁白、水印
     *
     * @param id 编号
     */
    void recomposeProject(Long id);

    /**
     * 获得成片的生成任务分页：有成片状态的项目
     *
     * @param pageParam 分页参数
     * @param reqVO 筛选条件
     * @param projectIds 项目编号；为空表示不过滤
     * @return 分页
     */
    PageResult<AiDramaProjectDO> getComposeTaskPage(PageParam pageParam, AiDramaTaskPageReqVO reqVO,
                                                    Collection<Long> projectIds);

    /**
     * 把合成中的成片标记为已失败
     *
     * @param id 编号
     * @param errorMessage 失败原因
     */
    void updateComposeFail(Long id, String errorMessage);

    /**
     * 成片超时处理：合成中且早于指定时间未更新的，置为已失败（例如服务重启，异步任务丢失）
     *
     * @param updateTime 更新时间
     * @param errorMessage 失败原因
     * @return 处理的数量
     */
    int timeoutCompose(LocalDateTime updateTime, String errorMessage);

}
