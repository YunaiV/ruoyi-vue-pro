package cn.iocoder.yudao.module.im.service.sensitiveword;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.im.controller.admin.manager.sensitiveword.vo.ImSensitiveWordPageReqVO;
import cn.iocoder.yudao.module.im.controller.admin.manager.sensitiveword.vo.ImSensitiveWordSaveReqVO;
import cn.iocoder.yudao.module.im.dal.dataobject.sensitiveword.ImSensitiveWordDO;

import javax.validation.Valid;
import java.util.List;

/**
 * IM 敏感词 Service 接口
 *
 * @author 芋道源码
 */
public interface ImSensitiveWordService {

    /**
     * 校验文本是否包含敏感词
     *
     * @param text 待校验文本
     */
    void validateText(String text);

    // ==================== 管理后台 ====================

    /**
     * 【管理后台】创建敏感词
     *
     * @param createReqVO 创建信息
     * @return 敏感词编号
     */
    Long createSensitiveWord(@Valid ImSensitiveWordSaveReqVO createReqVO);

    /**
     * 【管理后台】更新敏感词
     *
     * @param updateReqVO 更新信息
     */
    void updateSensitiveWord(@Valid ImSensitiveWordSaveReqVO updateReqVO);

    /**
     * 【管理后台】删除敏感词
     *
     * @param id 敏感词编号
     */
    void deleteSensitiveWord(Long id);

    /**
     * 【管理后台】批量删除敏感词
     *
     * @param ids 敏感词编号列表
     */
    void deleteSensitiveWordList(List<Long> ids);

    /**
     * 【管理后台】获得敏感词
     *
     * @param id 敏感词编号
     * @return 敏感词
     */
    ImSensitiveWordDO getSensitiveWord(Long id);

    /**
     * 【管理后台】获得敏感词分页
     *
     * @param pageReqVO 分页查询
     * @return 敏感词分页
     */
    PageResult<ImSensitiveWordDO> getSensitiveWordPage(ImSensitiveWordPageReqVO pageReqVO);

}
