package cn.iocoder.yudao.module.mp.service.account;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.mp.admin.account.vo.WxAccountCreateReqVO;
import cn.iocoder.yudao.module.mp.admin.account.vo.WxAccountExportReqVO;
import cn.iocoder.yudao.module.mp.admin.account.vo.WxAccountPageReqVO;
import cn.iocoder.yudao.module.mp.admin.account.vo.WxAccountUpdateReqVO;
import cn.iocoder.yudao.module.mp.dal.dataobject.account.WxAccountDO;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

/**
 * 公众号账户 Service 接口
 *
 * @author 芋道源码
 */
public interface WxAccountService {

    /**
     * 创建公众号账户
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createWxAccount(@Valid WxAccountCreateReqVO createReqVO);

    /**
     * 更新公众号账户
     *
     * @param updateReqVO 更新信息
     */
    void updateWxAccount(@Valid WxAccountUpdateReqVO updateReqVO);

    /**
     * 删除公众号账户
     *
     * @param id 编号
     */
    void deleteWxAccount(Long id);

    /**
     * 获得公众号账户
     *
     * @param id 编号
     * @return 公众号账户
     */
    WxAccountDO getWxAccount(Long id);

    /**
     * 获得公众号账户列表
     *
     * @param ids 编号
     * @return 公众号账户列表
     */
    List<WxAccountDO> getWxAccountList(Collection<Long> ids);

    /**
     * 获得公众号账户分页
     *
     * @param pageReqVO 分页查询
     * @return 公众号账户分页
     */
    PageResult<WxAccountDO> getWxAccountPage(WxAccountPageReqVO pageReqVO);

    /**
     * 获得公众号账户列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 公众号账户列表
     */
    List<WxAccountDO> getWxAccountList(WxAccountExportReqVO exportReqVO);

    /**
     * 查询账户
     *
     * @param field
     * @param val
     * @return
     */
    WxAccountDO findBy(SFunction<WxAccountDO, ?> field, Object val);
}
