package cn.iocoder.yudao.module.product.service.comment;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.product.controller.admin.comment.vo.ProductCommentPageReqVO;
import cn.iocoder.yudao.module.product.controller.admin.comment.vo.ProductCommentReplyVO;
import cn.iocoder.yudao.module.product.controller.admin.comment.vo.ProductCommentUpdateVisibleReqVO;
import cn.iocoder.yudao.module.product.controller.app.comment.vo.AppCommentPageReqVO;
import cn.iocoder.yudao.module.product.controller.app.comment.vo.AppCommentRespVO;
import cn.iocoder.yudao.module.product.dal.dataobject.comment.ProductCommentDO;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Map;

/**
 * 商品评论 Service 接口
 *
 * @author wangzhs
 */
@Service
@Validated
public interface ProductCommentService {

    /**
     * 获得商品评价分页
     *
     * @param pageReqVO 分页查询
     * @return 商品评价分页
     */
    PageResult<ProductCommentDO> getCommentPage(ProductCommentPageReqVO pageReqVO);

    /**
     * 修改评论是否可见
     *
     * @param updateReqVO 修改评论可见
     */
    void updateCommentVisible(ProductCommentUpdateVisibleReqVO updateReqVO);

    // TODO @puhui999：replyComment
    /**
     * 商家回复
     *
     * @param replyVO     商家回复
     * @param loginUserId 管理后台商家登陆人 ID
     */
    void commentReply(ProductCommentReplyVO replyVO, Long loginUserId);

    /**
     * 获得商品评价分页
     *
     * @param pageVO  分页查询
     * @param visible 是否可见
     * @return 商品评价分页
     */
    PageResult<AppCommentRespVO> getCommentPage(AppCommentPageReqVO pageVO, Boolean visible);

    /**
     * 创建商品评论
     *
     * @param productComment 创建实体
     * @param system         是否系统评价
     */
    void createComment(ProductCommentDO productComment, Boolean system);

    /**
     * 获得商品的评价统计
     *
     * @param spuId   spu id
     * @param visible 是否可见
     * @return 评价统计
     */
    Map<String, Long> getCommentPageTabsCount(Long spuId, Boolean visible);

}
