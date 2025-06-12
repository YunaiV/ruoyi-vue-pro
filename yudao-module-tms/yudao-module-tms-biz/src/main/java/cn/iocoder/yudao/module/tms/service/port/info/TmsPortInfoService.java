package cn.iocoder.yudao.module.tms.service.port.info;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.tms.controller.admin.port.info.vo.TmsPortInfoPageReqVO;
import cn.iocoder.yudao.module.tms.controller.admin.port.info.vo.TmsPortInfoSaveReqVO;
import cn.iocoder.yudao.module.tms.dal.dataobject.port.info.TmsPortInfoDO;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Set;

/**
 * TMS港口信息 Service 接口
 *
 * @author wdy
 */
public interface TmsPortInfoService {

    /**
     * 创建TMS港口信息
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createPortInfo(@Valid TmsPortInfoSaveReqVO createReqVO);

    /**
     * 更新TMS港口信息
     *
     * @param updateReqVO 更新信息
     */
    void updatePortInfo(@Valid TmsPortInfoSaveReqVO updateReqVO);

    /**
     * 删除TMS港口信息
     *
     * @param id 编号
     */
    void deletePortInfo(Long id);

    /**
     * 获得TMS港口信息
     *
     * @param id 编号
     * @return TMS港口信息
     */
    TmsPortInfoDO getPortInfo(Long id);

    /**
     * 获得TMS港口信息分页
     *
     * @param pageReqVO 分页查询
     * @return TMS港口信息分页
     */
    PageResult<TmsPortInfoDO> getPortInfoPage(TmsPortInfoPageReqVO pageReqVO);

    /**
     * 获得TMS港口信息列表
     *
     * @return TMS港口信息列表
     */
    List<TmsPortInfoDO> getPortInfoList();

    /**
     * 校验港口是否存在
     *
     * @param id 编号
     * @return 港口信息
     */
    TmsPortInfoDO validatePortInfoExists(Long id);

    /**
     * 批量校验港口是否存在
     *
     * @param ids 编号集合
     * @return 港口信息列表
     */
    List<TmsPortInfoDO> validatePortInfoExistsList(Set<Long> ids);

}