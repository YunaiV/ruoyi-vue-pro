package cn.iocoder.yudao.module.jl.service.join;

import java.util.*;
import javax.validation.*;
import cn.iocoder.yudao.module.jl.controller.admin.join.vo.*;
import cn.iocoder.yudao.module.jl.dal.dataobject.join.JoinCustomer2saleDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

/**
 * 客户所属的销售人员 Service 接口
 *
 * @author 惟象科技
 */
public interface JoinCustomer2saleService {

    /**
     * 创建客户所属的销售人员
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createJoinCustomer2sale(@Valid JoinCustomer2saleCreateReqVO createReqVO);

    /**
     * 更新客户所属的销售人员
     *
     * @param updateReqVO 更新信息
     */
    void updateJoinCustomer2sale(@Valid JoinCustomer2saleUpdateReqVO updateReqVO);

    /**
     * 删除客户所属的销售人员
     *
     * @param id 编号
     */
    void deleteJoinCustomer2sale(Long id);

    /**
     * 获得客户所属的销售人员
     *
     * @param id 编号
     * @return 客户所属的销售人员
     */
    JoinCustomer2saleDO getJoinCustomer2sale(Long id);

    /**
     * 获得客户所属的销售人员列表
     *
     * @param ids 编号
     * @return 客户所属的销售人员列表
     */
    List<JoinCustomer2saleDO> getJoinCustomer2saleList(Collection<Long> ids);

    /**
     * 获得客户所属的销售人员分页
     *
     * @param pageReqVO 分页查询
     * @return 客户所属的销售人员分页
     */
    PageResult<JoinCustomer2saleDO> getJoinCustomer2salePage(JoinCustomer2salePageReqVO pageReqVO);

    /**
     * 获得客户所属的销售人员列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 客户所属的销售人员列表
     */
    List<JoinCustomer2saleDO> getJoinCustomer2saleList(JoinCustomer2saleExportReqVO exportReqVO);

}
