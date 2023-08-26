package cn.iocoder.yudao.module.product.service.comment;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.product.api.comment.dto.ProductCommentCreateReqDTO;
import cn.iocoder.yudao.module.product.controller.admin.comment.vo.ProductCommentCreateReqVO;
import cn.iocoder.yudao.module.product.controller.admin.comment.vo.ProductCommentPageReqVO;
import cn.iocoder.yudao.module.product.controller.admin.comment.vo.ProductCommentReplyReqVO;
import cn.iocoder.yudao.module.product.controller.admin.comment.vo.ProductCommentUpdateVisibleReqVO;
import cn.iocoder.yudao.module.product.controller.app.comment.vo.AppCommentPageReqVO;
import cn.iocoder.yudao.module.product.controller.app.comment.vo.AppCommentStatisticsRespVO;
import cn.iocoder.yudao.module.product.controller.app.comment.vo.AppProductCommentRespVO;
import cn.iocoder.yudao.module.product.dal.dataobject.comment.ProductCommentDO;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * 商品评论 Service 接口
 *
 * @author wangzhs
 */
@Service
@Validated
public interface ProductCommentService {

    /**
     * 创建商品评论
     * 后台管理员创建评论使用
     *
     * @param createReqVO 商品评价创建 Request VO 对象
     */
    void createComment(ProductCommentCreateReqVO createReqVO);

    /**
     * 创建评论
     * 创建商品评论 APP 端创建商品评论使用
     *
     * @param createReqDTO 创建请求 dto
     * @return 返回评论 id
     */
    Long createComment(ProductCommentCreateReqDTO createReqDTO);

    /**
     * 修改评论是否可见
     *
     * @param updateReqVO 修改评论可见
     */
    void updateCommentVisible(ProductCommentUpdateVisibleReqVO updateReqVO);

    /**
     * 商家回复
     *
     * @param replyVO     商家回复
     * @param userId 管理后台商家登陆人 ID
     */
    void replyComment(ProductCommentReplyReqVO replyVO, Long userId);

    /**
     * 【管理员】获得商品评价分页
     *
     * @param pageReqVO 分页查询
     * @return 商品评价分页
     */
    PageResult<ProductCommentDO> getCommentPage(ProductCommentPageReqVO pageReqVO);

    /**
     * 【会员】获得商品评价分页
     *
     * @param pageVO  分页查询
     * @param visible 是否可见
     * @return 商品评价分页
     */
    PageResult<ProductCommentDO> getCommentPage(AppCommentPageReqVO pageVO, Boolean visible);

    /**
     * 获得商品的评价统计
     *
     * @param spuId   spu id
     * @param visible 是否可见
     * @return 评价统计
     */
    AppCommentStatisticsRespVO getCommentStatistics(Long spuId, Boolean visible);

    /**
     * 得到评论列表
     *
     * @param spuId 商品 id
     * @param count 数量
     * @return {@link Object}
     */
    List<AppProductCommentRespVO> getCommentList(Long spuId, Integer count);

}
