package cn.iocoder.dashboard.modules.infra.service.errorcode;

import cn.iocoder.dashboard.common.pojo.PageResult;
import cn.iocoder.dashboard.framework.errorcode.core.service.ErrorCodeFrameworkService;
import cn.iocoder.dashboard.modules.infra.controller.errorcode.vo.InfErrorCodeCreateReqVO;
import cn.iocoder.dashboard.modules.infra.controller.errorcode.vo.InfErrorCodeExportReqVO;
import cn.iocoder.dashboard.modules.infra.controller.errorcode.vo.InfErrorCodePageReqVO;
import cn.iocoder.dashboard.modules.infra.controller.errorcode.vo.InfErrorCodeUpdateReqVO;
import cn.iocoder.dashboard.modules.infra.dal.dataobject.errorcode.InfErrorCodeDO;

import javax.validation.Valid;
import java.util.List;

/**
 * 错误码 Service 接口
 *
 * @author 芋道源码
 */
public interface InfErrorCodeService extends ErrorCodeFrameworkService {

    /**
     * 创建错误码
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createErrorCode(@Valid InfErrorCodeCreateReqVO createReqVO);

    /**
     * 更新错误码
     *
     * @param updateReqVO 更新信息
     */
    void updateErrorCode(@Valid InfErrorCodeUpdateReqVO updateReqVO);

    /**
     * 删除错误码
     *
     * @param id 编号
     */
    void deleteErrorCode(Long id);

    /**
     * 获得错误码
     *
     * @param id 编号
     * @return 错误码
     */
    InfErrorCodeDO getErrorCode(Long id);

    /**
     * 获得错误码分页
     *
     * @param pageReqVO 分页查询
     * @return 错误码分页
     */
    PageResult<InfErrorCodeDO> getErrorCodePage(InfErrorCodePageReqVO pageReqVO);

    /**
     * 获得错误码列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 错误码列表
     */
    List<InfErrorCodeDO> getErrorCodeList(InfErrorCodeExportReqVO exportReqVO);

}
