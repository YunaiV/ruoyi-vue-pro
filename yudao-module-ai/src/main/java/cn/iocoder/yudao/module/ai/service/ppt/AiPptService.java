package cn.iocoder.yudao.module.ai.service.ppt;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.ai.controller.admin.ppt.vo.AiPptGenerateReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.ppt.vo.AiPptPageReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.ppt.vo.AiPptRedrawReqVO;
import cn.iocoder.yudao.module.ai.controller.admin.ppt.vo.AiPptSlideImageReqVO;
import cn.iocoder.yudao.module.ai.dal.dataobject.ppt.AiPptDO;

/**
 * AI PPT Service 接口
 *
 * @author 芋道源码
 */
public interface AiPptService {

    // ========== 创作 ==========

    /**
     * 生成 PPT：插入生成中的记录后，异步生成分镜、逐页生图
     *
     * @param userId 用户编号
     * @param reqVO 生成信息
     * @return 编号
     */
    Long generatePpt(Long userId, AiPptGenerateReqVO reqVO);

    /**
     * 继续生成【我的】已失败的 PPT：没有分镜时重新生成分镜，否则只补没有图的页
     *
     * @param id 编号
     * @param userId 用户编号
     */
    void resumePptMy(Long id, Long userId);

    /**
     * 重绘【我的】PPT 的一页：使用修改后的完整提示词，异步文生图
     *
     * @param reqVO 重绘信息
     * @param userId 用户编号
     */
    void redrawSlideMy(AiPptRedrawReqVO reqVO, Long userId);

    /**
     * 切换【我的】PPT 一页的当前图片为历史版本之一
     *
     * @param reqVO 切换信息
     * @param userId 用户编号
     */
    void updateSlideImageMy(AiPptSlideImageReqVO reqVO, Long userId);

    // ========== 我的 PPT ==========

    /**
     * 获得【我的】PPT 分页
     *
     * @param userId 用户编号
     * @param pageReqVO 分页查询
     * @return PPT 分页
     */
    PageResult<AiPptDO> getPptMyPage(Long userId, PageParam pageReqVO);

    /**
     * 获得【我的】PPT：不是本人的，按不存在处理
     *
     * @param id 编号
     * @param userId 用户编号
     * @return PPT
     */
    AiPptDO getPptMy(Long id, Long userId);

    /**
     * 删除【我的】PPT：生成中的不允许删除
     *
     * @param id 编号
     * @param userId 用户编号
     */
    void deletePptMy(Long id, Long userId);

    /**
     * 导出 PPT：每页一张整页图，按需生成，不保存导出文件
     *
     * @param ppt PPT
     * @param format 格式：pptx、pdf
     * @return 文件内容
     */
    byte[] exportPpt(AiPptDO ppt, String format);

    // ========== PPT 管理 ==========

    /**
     * 获得 PPT 分页
     *
     * @param pageReqVO 分页查询
     * @return PPT 分页
     */
    PageResult<AiPptDO> getPptPage(AiPptPageReqVO pageReqVO);

    /**
     * 获得 PPT
     *
     * @param id 编号
     * @return PPT
     */
    AiPptDO getPpt(Long id);

    /**
     * 校验 PPT 存在
     *
     * @param id 编号
     * @return PPT
     */
    AiPptDO validatePptExists(Long id);

    /**
     * 删除 PPT：生成中的不允许删除
     *
     * @param id 编号
     */
    void deletePpt(Long id);

    // ========== 超时 ==========

    /**
     * 超时处理：生成中、且超过时限没有进展的，结束生成
     *
     * @return 处理的数量
     */
    int timeoutPpt();

}
