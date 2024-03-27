package cn.iocoder.yudao.module.mp.service.tag;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.mp.controller.admin.tag.vo.MpTagCreateReqVO;
import cn.iocoder.yudao.module.mp.controller.admin.tag.vo.MpTagPageReqVO;
import cn.iocoder.yudao.module.mp.controller.admin.tag.vo.MpTagUpdateReqVO;
import cn.iocoder.yudao.module.mp.dal.dataobject.tag.MpTagDO;

import javax.validation.Valid;
import java.util.List;

/**
 * 公众号标签 Service 接口
 *
 * @author fengdan
 */
public interface MpTagService {

    /**
     * 创建公众号标签
     *
     * @param createReqVO 创建标签信息
     * @return 标签编号
     */
    Long createTag(@Valid MpTagCreateReqVO createReqVO);

    /**
     * 更新公众号标签
     *
     * @param updateReqVO 更新标签信息
     */
    void updateTag(@Valid MpTagUpdateReqVO updateReqVO);

    /**
     * 删除公众号标签
     *
     * @param id    编号
     */
    void deleteTag(Long id);

    /**
     * 获得公众号标签分页
     *
     * @param pageReqVO 分页查询
     * @return 公众号标签分页
     */
    PageResult<MpTagDO> getTagPage(MpTagPageReqVO pageReqVO);

    /**
     * 获得公众号标签详情
     * @param id id查询
     * @return 公众号标签详情
     */
    MpTagDO get(Long id);

    List<MpTagDO> getTagList();

    /**
     * 同步公众号标签
     *
     * @param accountId 公众号账号的编号
     */
    void syncTag(Long accountId);

}
