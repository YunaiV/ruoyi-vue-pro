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
     * 【管理后台】分页查询敏感词
     */
    PageResult<ImSensitiveWordDO> getSensitiveWordPage(ImSensitiveWordPageReqVO reqVO);

    /**
     * 【管理后台】获取敏感词详情
     */
    ImSensitiveWordDO getSensitiveWord(Long id);

    /**
     * 【管理后台】新增敏感词，返回新增 id
     */
    Long createSensitiveWord(@Valid ImSensitiveWordSaveReqVO reqVO);

    /**
     * 【管理后台】修改敏感词
     */
    void updateSensitiveWord(@Valid ImSensitiveWordSaveReqVO reqVO);

    /**
     * 【管理后台】删除敏感词
     */
    void deleteSensitiveWord(Long id);

    /**
     * 【管理后台】批量删除敏感词
     */
    void deleteSensitiveWordList(List<Long> ids);

}
