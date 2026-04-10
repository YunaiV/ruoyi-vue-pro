package cn.iocoder.yudao.module.mes.service.wm.outsourceissue;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.mes.controller.admin.wm.outsourceissue.vo.line.MesWmOutsourceIssueLinePageReqVO;
import cn.iocoder.yudao.module.mes.controller.admin.wm.outsourceissue.vo.line.MesWmOutsourceIssueLineSaveReqVO;
import cn.iocoder.yudao.module.mes.dal.dataobject.wm.outsourceissue.MesWmOutsourceIssueLineDO;
import jakarta.validation.Valid;

import java.util.List;

/**
 * MES 外协发料单行 Service 接口
 *
 * @author 芋道源码
 */
public interface MesWmOutsourceIssueLineService {

    /**
     * 创建外协发料单行
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createOutsourceIssueLine(@Valid MesWmOutsourceIssueLineSaveReqVO createReqVO);

    /**
     * 修改外协发料单行
     *
     * @param updateReqVO 修改信息
     */
    void updateOutsourceIssueLine(@Valid MesWmOutsourceIssueLineSaveReqVO updateReqVO);

    /**
     * 删除外协发料单行
     *
     * @param id 编号
     */
    void deleteOutsourceIssueLine(Long id);

    /**
     * 获得外协发料单行
     *
     * @param id 编号
     * @return 外协发料单行
     */
    MesWmOutsourceIssueLineDO getOutsourceIssueLine(Long id);

    /**
     * 获得外协发料单行分页
     *
     * @param pageReqVO 分页参数
     * @return 外协发料单行分页
     */
    PageResult<MesWmOutsourceIssueLineDO> getOutsourceIssueLinePage(MesWmOutsourceIssueLinePageReqVO pageReqVO);

    /**
     * 获得外协发料单行列表
     *
     * @param issueId 发料单ID
     * @return 外协发料单行列表
     */
    List<MesWmOutsourceIssueLineDO> getOutsourceIssueLineListByIssueId(Long issueId);

    /**
     * 删除外协发料单行
     *
     * @param issueId 发料单ID
     */
    void deleteOutsourceIssueLineByIssueId(Long issueId);

}
