package cn.iocoder.yudao.module.mp.service.receivetext;

import java.util.*;
import javax.validation.*;

import cn.iocoder.yudao.module.mp.controller.admin.receivetext.vo.*;
import cn.iocoder.yudao.module.mp.dal.dataobject.receivetext.WxReceiveTextDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

/**
 * 回复关键字 Service 接口
 *
 * @author 芋道源码
 */
public interface WxReceiveTextService {

    /**
     * 创建回复关键字
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Integer createWxReceiveText(@Valid WxReceiveTextCreateReqVO createReqVO);

    /**
     * 更新回复关键字
     *
     * @param updateReqVO 更新信息
     */
    void updateWxReceiveText(@Valid WxReceiveTextUpdateReqVO updateReqVO);

    /**
     * 删除回复关键字
     *
     * @param id 编号
     */
    void deleteWxReceiveText(Integer id);

    /**
     * 获得回复关键字
     *
     * @param id 编号
     * @return 回复关键字
     */
    WxReceiveTextDO getWxReceiveText(Integer id);

    /**
     * 获得回复关键字列表
     *
     * @param ids 编号
     * @return 回复关键字列表
     */
    List<WxReceiveTextDO> getWxReceiveTextList(Collection<Integer> ids);

    /**
     * 获得回复关键字分页
     *
     * @param pageReqVO 分页查询
     * @return 回复关键字分页
     */
    PageResult<WxReceiveTextDO> getWxReceiveTextPage(WxReceiveTextPageReqVO pageReqVO);

    /**
     * 获得回复关键字列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 回复关键字列表
     */
    List<WxReceiveTextDO> getWxReceiveTextList(WxReceiveTextExportReqVO exportReqVO);

}
