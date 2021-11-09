package cn.iocoder.yudao.adminserver.modules.pay.service.app;

import java.util.*;
import javax.validation.*;

import cn.iocoder.yudao.adminserver.modules.pay.controller.app.vo.*;
import cn.iocoder.yudao.coreservice.modules.pay.dal.dataobject.merchant.PayAppDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import org.springframework.web.multipart.MultipartFile;

/**
 * 支付应用信息 Service 接口
 *
 * @author 芋艿
 */
public interface PayAppService {

    /**
     * 创建支付应用信息
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createApp(@Valid PayAppCreateReqVO createReqVO);

    /**
     * 更新支付应用信息
     *
     * @param updateReqVO 更新信息
     */
    void updateApp(@Valid PayAppUpdateReqVO updateReqVO);

    /**
     * 删除支付应用信息
     *
     * @param id 编号
     */
    void deleteApp(Long id);

    /**
     * 获得支付应用信息
     *
     * @param id 编号
     * @return 支付应用信息
     */
    PayAppDO getApp(Long id);

    /**
     * 获得支付应用信息列表
     *
     * @param ids 编号
     * @return 支付应用信息列表
     */
    List<PayAppDO> getAppList(Collection<Long> ids);

    /**
     * 获得支付应用信息分页
     *
     * @param pageReqVO 分页查询
     * @return 支付应用信息分页
     */
    PageResult<PayAppDO> getAppPage(PayAppPageReqVO pageReqVO);

    /**
     * 获得支付应用信息列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 支付应用信息列表
     */
    List<PayAppDO> getAppList(PayAppExportReqVO exportReqVO);

    /**
     * 修改应用信息状态
     *
     * @param id     应用编号
     * @param status 状态{@link cn.iocoder.yudao.framework.common.enums.CommonStatusEnum}
     */
    void updateAppStatus(Long id, Integer status);


}
