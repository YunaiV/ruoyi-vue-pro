package cn.iocoder.yudao.module.system.service.errorcode;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.system.framework.errorcode.core.service.ErrorCodeFrameworkService;
import cn.iocoder.yudao.module.system.controller.errorcode.vo.SysErrorCodeCreateReqVO;
import cn.iocoder.yudao.module.system.controller.errorcode.vo.SysErrorCodeExportReqVO;
import cn.iocoder.yudao.module.system.controller.errorcode.vo.SysErrorCodePageReqVO;
import cn.iocoder.yudao.module.system.controller.errorcode.vo.SysErrorCodeUpdateReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.errorcode.SysErrorCodeDO;

import javax.validation.Valid;
import java.util.List;

/**
 * 错误码 Service 接口
 *
 * @author 芋道源码
 */
public interface SysErrorCodeService extends ErrorCodeFrameworkService {

    /**
     * 创建错误码
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createErrorCode(@Valid SysErrorCodeCreateReqVO createReqVO);

    /**
     * 更新错误码
     *
     * @param updateReqVO 更新信息
     */
    void updateErrorCode(@Valid SysErrorCodeUpdateReqVO updateReqVO);

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
    SysErrorCodeDO getErrorCode(Long id);

    /**
     * 获得错误码分页
     *
     * @param pageReqVO 分页查询
     * @return 错误码分页
     */
    PageResult<SysErrorCodeDO> getErrorCodePage(SysErrorCodePageReqVO pageReqVO);

    /**
     * 获得错误码列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 错误码列表
     */
    List<SysErrorCodeDO> getErrorCodeList(SysErrorCodeExportReqVO exportReqVO);

}
