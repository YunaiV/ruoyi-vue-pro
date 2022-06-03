package cn.iocoder.yudao.module.mp.service.fansmsgres;

import java.util.*;
import javax.validation.*;

import cn.iocoder.yudao.module.mp.controller.admin.fansmsgres.vo.*;
import cn.iocoder.yudao.module.mp.dal.dataobject.fansmsgres.WxFansMsgResDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

/**
 * 回复粉丝消息历史表  Service 接口
 *
 * @author 芋道源码
 */
public interface WxFansMsgResService {

    /**
     * 创建回复粉丝消息历史表
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Integer createWxFansMsgRes(@Valid WxFansMsgResCreateReqVO createReqVO);

    /**
     * 更新回复粉丝消息历史表
     *
     * @param updateReqVO 更新信息
     */
    void updateWxFansMsgRes(@Valid WxFansMsgResUpdateReqVO updateReqVO);

    /**
     * 删除回复粉丝消息历史表
     *
     * @param id 编号
     */
    void deleteWxFansMsgRes(Integer id);

    /**
     * 获得回复粉丝消息历史表
     *
     * @param id 编号
     * @return 回复粉丝消息历史表
     */
    WxFansMsgResDO getWxFansMsgRes(Integer id);

    /**
     * 获得回复粉丝消息历史表 列表
     *
     * @param ids 编号
     * @return 回复粉丝消息历史表 列表
     */
    List<WxFansMsgResDO> getWxFansMsgResList(Collection<Integer> ids);

    /**
     * 获得回复粉丝消息历史表 分页
     *
     * @param pageReqVO 分页查询
     * @return 回复粉丝消息历史表 分页
     */
    PageResult<WxFansMsgResDO> getWxFansMsgResPage(WxFansMsgResPageReqVO pageReqVO);

    /**
     * 获得回复粉丝消息历史表 列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 回复粉丝消息历史表 列表
     */
    List<WxFansMsgResDO> getWxFansMsgResList(WxFansMsgResExportReqVO exportReqVO);

}
