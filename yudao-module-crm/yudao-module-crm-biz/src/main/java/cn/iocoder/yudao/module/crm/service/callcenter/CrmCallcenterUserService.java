package cn.iocoder.yudao.module.crm.service.callcenter;

import javax.validation.*;
import cn.iocoder.yudao.module.crm.controller.admin.callcenter.vo.CrmCallcenterUserPageReqVO;
import cn.iocoder.yudao.module.crm.controller.admin.callcenter.vo.CrmCallcenterUserRespVO;
import cn.iocoder.yudao.module.crm.controller.admin.callcenter.vo.CrmCallcenterUserSaveReqVO;
import cn.iocoder.yudao.module.crm.dal.dataobject.callcenter.CrmCallcenterUserDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

import java.util.List;

/**
 * 用户与呼叫中心用户绑定关系 Service 接口
 *
 * @author inao
 */
public interface CrmCallcenterUserService {

    /**
     * 创建用户与呼叫中心用户绑定关系
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createCallcenterUser(@Valid CrmCallcenterUserSaveReqVO createReqVO);

    /**
     * 更新用户与呼叫中心用户绑定关系
     *
     * @param updateReqVO 更新信息
     */
    void updateCallcenterUser(@Valid CrmCallcenterUserSaveReqVO updateReqVO);

    /**
     * 删除用户与呼叫中心用户绑定关系
     *
     * @param id 编号
     */
    void deleteCallcenterUser(Long id);

    /**
     * 获得用户与呼叫中心用户绑定关系
     *
     * @param id 编号
     * @return 用户与呼叫中心用户绑定关系
     */
    CrmCallcenterUserDO getCallcenterUser(Long id);

    /**
     * 获得用户与呼叫中心用户绑定关系分页
     *
     * @param pageReqVO 分页查询
     * @return 用户与呼叫中心用户绑定关系分页
     */
    PageResult<CrmCallcenterUserDO> getCallcenterUserPage(CrmCallcenterUserPageReqVO pageReqVO);


    /**
     *  获取未绑定的用户列表
     *  @return 反绑定用户清单
     */
    List<CrmCallcenterUserDO> getList();

}