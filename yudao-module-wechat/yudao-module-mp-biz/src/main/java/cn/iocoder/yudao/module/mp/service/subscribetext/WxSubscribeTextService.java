package cn.iocoder.yudao.module.mp.service.subscribetext;

import java.util.*;
import javax.validation.*;

import cn.iocoder.yudao.module.mp.controller.admin.subscribetext.vo.*;
import cn.iocoder.yudao.module.mp.dal.dataobject.account.WxAccountDO;
import cn.iocoder.yudao.module.mp.dal.dataobject.subscribetext.WxSubscribeTextDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;

/**
 * 关注欢迎语 Service 接口
 *
 * @author 芋道源码
 */
public interface WxSubscribeTextService {

    /**
     * 创建关注欢迎语
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Integer createWxSubscribeText(@Valid WxSubscribeTextCreateReqVO createReqVO);

    /**
     * 更新关注欢迎语
     *
     * @param updateReqVO 更新信息
     */
    void updateWxSubscribeText(@Valid WxSubscribeTextUpdateReqVO updateReqVO);

    /**
     * 删除关注欢迎语
     *
     * @param id 编号
     */
    void deleteWxSubscribeText(Integer id);

    /**
     * 获得关注欢迎语
     *
     * @param id 编号
     * @return 关注欢迎语
     */
    WxSubscribeTextDO getWxSubscribeText(Integer id);

    /**
     * 获得关注欢迎语列表
     *
     * @param ids 编号
     * @return 关注欢迎语列表
     */
    List<WxSubscribeTextDO> getWxSubscribeTextList(Collection<Integer> ids);

    /**
     * 获得关注欢迎语分页
     *
     * @param pageReqVO 分页查询
     * @return 关注欢迎语分页
     */
    PageResult<WxSubscribeTextDO> getWxSubscribeTextPage(WxSubscribeTextPageReqVO pageReqVO);

    /**
     * 获得关注欢迎语列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 关注欢迎语列表
     */
    List<WxSubscribeTextDO> getWxSubscribeTextList(WxSubscribeTextExportReqVO exportReqVO);

    /**
     * 关注欢迎语分页
     *
     * @param column
     * @param obj
     * @return
     */
    WxSubscribeTextDO findBy(SFunction<WxSubscribeTextDO, ?> column, Object obj);
}
