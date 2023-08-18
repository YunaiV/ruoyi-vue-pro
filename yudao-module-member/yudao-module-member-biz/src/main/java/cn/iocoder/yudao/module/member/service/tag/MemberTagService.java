package cn.iocoder.yudao.module.member.service.tag;

import java.util.*;
import javax.validation.*;
import cn.iocoder.yudao.module.member.controller.admin.tag.vo.*;
import cn.iocoder.yudao.module.member.dal.dataobject.tag.MemberTagDO;
import cn.iocoder.yudao.framework.common.pojo.PageResult;

/**
 * 会员标签 Service 接口
 *
 * @author 芋道源码
 */
public interface MemberTagService {

    /**
     * 创建会员标签
     *
     * @param createReqVO 创建信息
     * @return 编号
     */
    Long createTag(@Valid MemberTagCreateReqVO createReqVO);

    /**
     * 更新会员标签
     *
     * @param updateReqVO 更新信息
     */
    void updateTag(@Valid MemberTagUpdateReqVO updateReqVO);

    /**
     * 删除会员标签
     *
     * @param id 编号
     */
    void deleteTag(Long id);

    /**
     * 获得会员标签
     *
     * @param id 编号
     * @return 会员标签
     */
    MemberTagDO getTag(Long id);

    /**
     * 获得会员标签列表
     *
     * @param ids 编号
     * @return 会员标签列表
     */
    List<MemberTagDO> getTagList(Collection<Long> ids);

    /**
     * 获得会员标签分页
     *
     * @param pageReqVO 分页查询
     * @return 会员标签分页
     */
    PageResult<MemberTagDO> getTagPage(MemberTagPageReqVO pageReqVO);

    /**
     * 获得会员标签列表, 用于 Excel 导出
     *
     * @param exportReqVO 查询条件
     * @return 会员标签列表
     */
    List<MemberTagDO> getTagList(MemberTagExportReqVO exportReqVO);

}
