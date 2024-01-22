package cn.iocoder.yudao.module.member.service.group;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.member.controller.admin.group.vo.MemberGroupCreateReqVO;
import cn.iocoder.yudao.module.member.controller.admin.group.vo.MemberGroupPageReqVO;
import cn.iocoder.yudao.module.member.controller.admin.group.vo.MemberGroupUpdateReqVO;
import cn.iocoder.yudao.module.member.dal.dataobject.group.MemberGroupDO;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;

/**
 * 用户分组 Service 接口
 *
 * @author owen
 */
public interface MemberGroupService {

    /**
     * 创建用户分组
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createGroup(@Valid MemberGroupCreateReqVO createReqVO);

    /**
     * 更新用户分组
     *
     * @param updateReqVO 更新信息
     */
    void updateGroup(@Valid MemberGroupUpdateReqVO updateReqVO);

    /**
     * 删除用户分组
     *
     * @param id 编号
     */
    void deleteGroup(Long id);

    /**
     * 获得用户分组
     *
     * @param id 编号
     * @return 用户分组
     */
    MemberGroupDO getGroup(Long id);

    /**
     * 获得用户分组列表
     *
     * @param ids 编号
     * @return 用户分组列表
     */
    List<MemberGroupDO> getGroupList(Collection<Long> ids);

    /**
     * 获得用户分组分页
     *
     * @param pageReqVO 分页查询
     * @return 用户分组分页
     */
    PageResult<MemberGroupDO> getGroupPage(MemberGroupPageReqVO pageReqVO);

    /**
     * 获得指定状态的用户分组列表
     *
     * @param status 状态
     * @return 用户分组列表
     */
    List<MemberGroupDO> getGroupListByStatus(Integer status);

    /**
     * 获得开启状态的用户分组列表
     *
     * @return 用户分组列表
     */
    default List<MemberGroupDO> getEnableGroupList() {
        return getGroupListByStatus(CommonStatusEnum.ENABLE.getStatus());
    }

}
