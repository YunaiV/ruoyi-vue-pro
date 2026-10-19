package cn.iocoder.yudao.module.pms.service.pm.project;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.pms.controller.admin.pm.project.vo.project.PmsProjectOverviewRespVO;
import cn.iocoder.yudao.module.pms.controller.admin.pm.project.vo.project.PmsProjectPageReqVO;
import cn.iocoder.yudao.module.pms.controller.admin.pm.project.vo.project.PmsProjectSaveReqVO;
import cn.iocoder.yudao.module.pms.dal.dataobject.pm.project.PmsProjectDO;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertMap;

/**
 * PMS 项目 Service 接口
 *
 * @author 芋道源码
 */
public interface PmsProjectService {

    /**
     * 创建项目
     *
     * @param saveReqVO 保存信息
     * @param userId 创建人编号
     * @return 项目编号
     */
    Long createProject(PmsProjectSaveReqVO saveReqVO, Long userId);

    /**
     * 更新项目
     *
     * @param saveReqVO 保存信息
     * @param userId 操作人编号
     */
    void updateProject(PmsProjectSaveReqVO saveReqVO, Long userId);

    /**
     * 获得项目
     *
     * @param id 项目编号
     * @return 项目
     */
    PmsProjectDO getProject(Long id);

    /**
     * 获得项目详情并记录访问时间
     *
     * @param id 项目编号
     * @param userId 当前用户编号
     * @return 项目
     */
    PmsProjectDO getProjectAndUpdateVisitTime(Long id, Long userId);

    /**
     * 获得项目分页
     *
     * @param pageReqVO 分页条件
     * @param userId 当前用户编号
     * @return 项目分页
     */
    PageResult<PmsProjectDO> getProjectPage(PmsProjectPageReqVO pageReqVO, Long userId);

    /**
     * 获得当前用户的星标项目列表
     *
     * @param userId 当前用户编号
     * @return 星标项目列表
     */
    List<PmsProjectDO> getFavoriteProjectList(Long userId);

    /**
     * 获得项目列表
     *
     * @param ids 项目编号集合
     * @return 项目列表
     */
    List<PmsProjectDO> getProjectList(Collection<Long> ids);

    /**
     * 获得项目 Map
     *
     * @param ids 项目编号集合
     * @return 项目 Map
     */
    default Map<Long, PmsProjectDO> getProjectMap(Collection<Long> ids) {
        return convertMap(getProjectList(ids), PmsProjectDO::getId);
    }

    /**
     * 获得项目概况
     *
     * @param id 项目编号
     * @param userId 当前用户编号
     * @return 项目概况
     */
    PmsProjectOverviewRespVO getProjectOverview(Long id, Long userId);

    /**
     * 归档项目
     *
     * @param id 项目编号
     * @param userId 操作人编号
     */
    void archiveProject(Long id, Long userId);

    /**
     * 将项目移入回收站
     *
     * @param id 项目编号
     * @param userId 操作人编号
     */
    void recycleProject(Long id, Long userId);

    /**
     * 恢复项目
     *
     * @param id 项目编号
     * @param userId 操作人编号
     */
    void restoreProject(Long id, Long userId);

    /**
     * 彻底删除回收站项目
     *
     * @param id 项目编号
     * @param userId 操作人编号
     */
    void deleteProject(Long id, Long userId);

}
