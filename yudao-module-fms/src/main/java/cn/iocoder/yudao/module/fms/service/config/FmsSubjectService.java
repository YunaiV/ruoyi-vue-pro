package cn.iocoder.yudao.module.fms.service.config;

import cn.iocoder.yudao.module.fms.controller.admin.config.vo.subject.FmsSubjectDeleteReqVO;
import cn.iocoder.yudao.module.fms.controller.admin.config.vo.subject.FmsSubjectImportExcelVO;
import cn.iocoder.yudao.module.fms.controller.admin.config.vo.subject.FmsSubjectImportRespVO;
import cn.iocoder.yudao.module.fms.controller.admin.config.vo.subject.FmsSubjectListReqVO;
import cn.iocoder.yudao.module.fms.controller.admin.config.vo.subject.FmsSubjectSaveReqVO;
import cn.iocoder.yudao.module.fms.controller.admin.config.vo.subject.FmsSubjectStatusReqVO;
import cn.iocoder.yudao.module.fms.controller.admin.config.vo.subject.FmsSubjectUsageRespVO;
import cn.iocoder.yudao.module.fms.dal.dataobject.config.FmsSubjectDO;

import java.util.List;

/**
 * FMS 会计科目 Service 接口
 *
 * @author 芋道源码
 */
public interface FmsSubjectService {

    /**
     * 初始化账套会计科目
     *
     * @param accountSetId 账套编号
     */
    void initializeDefaultSubjects(Long accountSetId);

    /**
     * 创建会计科目
     *
     * @param createReqVO 创建信息
     * @param userId 当前用户编号
     * @return 科目编号
     */
    Long createSubject(FmsSubjectSaveReqVO createReqVO, Long userId);

    /**
     * 更新会计科目
     *
     * @param updateReqVO 更新信息
     * @param userId 当前用户编号
     */
    void updateSubject(FmsSubjectSaveReqVO updateReqVO, Long userId);

    /**
     * 删除会计科目
     *
     * @param deleteReqVO 删除信息
     * @param userId 当前用户编号
     */
    void deleteSubjectList(FmsSubjectDeleteReqVO deleteReqVO, Long userId);

    /**
     * 更新会计科目状态
     *
     * @param statusReqVO 状态更新信息
     * @param userId 当前用户编号
     */
    void updateSubjectStatus(FmsSubjectStatusReqVO statusReqVO, Long userId);

    /**
     * 获得会计科目
     *
     * @param accountSetId 账套编号
     * @param id 科目编号
     * @param userId 当前用户编号
     * @return 会计科目
     */
    FmsSubjectDO getSubject(Long accountSetId, Long id, Long userId);

    /**
     * 获得会计科目使用情况
     *
     * @param accountSetId 账套编号
     * @param id 科目编号
     * @param userId 当前用户编号
     * @return 科目使用情况
     */
    FmsSubjectUsageRespVO getSubjectUsage(Long accountSetId, Long id, Long userId);

    /**
     * 获得账套下的会计科目列表
     *
     * @param accountSetId 账套编号
     * @param type 科目类型
     * @param userId 当前用户编号
     * @return 会计科目列表
     */
    List<FmsSubjectDO> getSubjectList(Long accountSetId, Integer type, Long userId);

    /**
     * 获得账套下的会计科目列表
     *
     * @param listReqVO 列表查询条件
     * @param userId 当前用户编号
     * @return 会计科目列表
     */
    List<FmsSubjectDO> getSubjectList(FmsSubjectListReqVO listReqVO, Long userId);

    /**
     * 获得科目及其所有下级科目编号
     *
     * @param accountSetId 账套编号
     * @param id 科目编号
     * @return 科目编号数组
     */
    List<Long> getSubjectIdListWithChildren(Long accountSetId, Long id);

    /**
     * 获得使用辅助核算类别的会计科目数量
     *
     * @param accountSetId 账套编号
     * @param auxiliaryTypeId 辅助核算类别编号
     * @return 会计科目数量
     */
    Long getSubjectCountByAuxiliaryTypeId(Long accountSetId, Long auxiliaryTypeId);

    /**
     * 获得使用币别的会计科目数量
     *
     * @param accountSetId 账套编号
     * @param currencyId 币别编号
     * @return 会计科目数量
     */
    Long getSubjectCountByCurrencyId(Long accountSetId, Long currencyId);

    /**
     * 按新的编码规则扩展会计科目编码
     *
     * @param accountSetId 账套编号
     * @param oldRules 原编码规则
     * @param newRules 新编码规则
     */
    void expandSubjectCodes(Long accountSetId, List<Integer> oldRules, List<Integer> newRules);

    /**
     * 导入会计科目
     *
     * @param accountSetId 账套编号
     * @param importSubjects 导入数据
     * @param userId 当前用户编号
     * @return 导入结果
     */
    FmsSubjectImportRespVO importSubjectList(Long accountSetId, List<FmsSubjectImportExcelVO> importSubjects,
                                            Long userId);

}
