package cn.iocoder.yudao.module.ai.service.drama;

import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.canvas.AiDramaCanvasSaveReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.drama.vo.canvas.AiDramaDirectorSaveReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.drama.AiDramaCanvasDO;
import cn.iocoder.yudao.module.ai.dal.dataobject.drama.AiDramaDirectorDO;

/**
 * AI 短剧画布 Service 接口：画布，以及从画布预演节点进入的 3D 导演工程
 *
 * @author 芋道源码
 */
public interface AiDramaCanvasService {

    /**
     * 获得我的项目的画布
     *
     * @param userId 用户编号
     * @param projectId 项目编号
     * @return 画布，未保存过时为空
     */
    AiDramaCanvasDO getCanvasMy(Long userId, Long projectId);

    /**
     * 保存我的项目的画布：修订号与当前不一致时拒绝
     *
     * @param userId 用户编号
     * @param reqVO 画布
     * @return 新的修订号
     */
    Long saveCanvasMy(Long userId, AiDramaCanvasSaveReqVO reqVO);

    /**
     * 获得我的项目的 3D 导演工程
     *
     * @param userId 用户编号
     * @param projectId 项目编号
     * @param nodeId 画布预演节点编号
     * @return 导演工程，未保存过时为空
     */
    AiDramaDirectorDO getDirectorMy(Long userId, Long projectId, String nodeId);

    /**
     * 保存我的项目的 3D 导演工程：修订号与当前不一致时拒绝
     *
     * @param userId 用户编号
     * @param reqVO 导演工程
     * @return 新的修订号
     */
    Long saveDirectorMy(Long userId, AiDramaDirectorSaveReqVO reqVO);

    /**
     * 删除项目的画布、3D 导演工程
     *
     * @param projectId 项目编号
     */
    void deleteCanvasByProjectId(Long projectId);

}
