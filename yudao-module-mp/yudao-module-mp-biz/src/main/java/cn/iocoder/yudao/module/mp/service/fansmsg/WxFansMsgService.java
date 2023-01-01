package cn.iocoder.yudao.module.mp.service.fansmsg;

import java.util.*;
import javax.validation.*;

import cn.iocoder.yudao.module.mp.controller.admin.fansmsg.vo.*;
import cn.iocoder.yudao.module.mp.dal.dataobject.fansmsg.WxFansMsgDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

/**
 * 粉丝消息表  Service 接口
 *
 * @author 芋道源码
 */
public interface WxFansMsgService {

    /**
     * 创建粉丝消息表
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Integer createWxFansMsg(@Valid WxFansMsgCreateReqVO createReqVO);

    /**
     * 更新粉丝消息表
     *
     * @param updateReqVO 更新信息
     */
    void updateWxFansMsg(@Valid WxFansMsgUpdateReqVO updateReqVO);

    /**
     * 删除粉丝消息表
     *
     * @param id 编号
     */
    void deleteWxFansMsg(Integer id);

    /**
     * 获得粉丝消息表
     *
     * @param id 编号
     * @return 粉丝消息表
     */
    WxFansMsgDO getWxFansMsg(Integer id);

    /**
     * 获得粉丝消息表 列表
     *
     * @param ids 编号
     * @return 粉丝消息表 列表
     */
    List<WxFansMsgDO> getWxFansMsgList(Collection<Integer> ids);

    /**
     * 获得粉丝消息表 分页
     *
     * @param pageReqVO 分页查询
     * @return 粉丝消息表 分页
     */
    PageResult<WxFansMsgDO> getWxFansMsgPage(WxFansMsgPageReqVO pageReqVO);

    /**
     * 获得粉丝消息表 列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 粉丝消息表 列表
     */
    List<WxFansMsgDO> getWxFansMsgList(WxFansMsgExportReqVO exportReqVO);

}
