package cn.iocoder.yudao.module.system.service.notice;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.system.controller.admin.notice.vo.NoticeCreateReqVO;
import cn.iocoder.yudao.module.system.controller.admin.notice.vo.NoticePageReqVO;
import cn.iocoder.yudao.module.system.controller.admin.notice.vo.NoticeUpdateReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.notice.NoticeDO;
import cn.iocoder.yudao.module.system.dal.mysql.notice.NoticeMapper;
import cn.iocoder.yudao.module.system.enums.notice.NoticeTypeEnum;
import cn.iocoder.yudao.framework.common.util.object.ObjectUtils;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;

import java.util.function.Consumer;

import static cn.hutool.core.util.RandomUtil.randomEle;
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.NOTICE_NOT_FOUND;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static org.junit.jupiter.api.Assertions.*;

@Import(NoticeServiceImpl.class)
class NoticeServiceImplTest extends BaseDbUnitTest {

    @Resource
    private NoticeServiceImpl sysNoticeService;

    @Resource
    private NoticeMapper sysNoticeMapper;

    @Test
    public void testPageNotices_success() {
        // 插入前置数据
        NoticeDO dbNotice = randomPojo(NoticeDO.class, o -> {
            o.setTitle("尼古拉斯赵四来啦！");
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
            o.setType(randomEle(NoticeTypeEnum.values()).getType());
        });
        sysNoticeMapper.insert(dbNotice);

        // 测试 title 不匹配
        sysNoticeMapper.insert(ObjectUtils.cloneIgnoreId(dbNotice, o -> o.setTitle("尼古拉斯凯奇也来啦！")));
        // 测试 status 不匹配
        sysNoticeMapper.insert(ObjectUtils.cloneIgnoreId(dbNotice, o -> o.setStatus(CommonStatusEnum.DISABLE.getStatus())));


        // 查询
        NoticePageReqVO reqVO = new NoticePageReqVO();
        reqVO.setTitle("尼古拉斯赵四来啦！");
        reqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());
        PageResult<NoticeDO> pageResult = sysNoticeService.pageNotices(reqVO);

        // 验证查询结果经过筛选
        assertEquals(1, pageResult.getTotal());
        assertEquals(1, pageResult.getList().size());
        assertPojoEquals(dbNotice, pageResult.getList().get(0));

    }

    @Test
    public void testGetNotice_success() {
        // 插入前置数据
        NoticeDO dbNotice = randomNoticeDO();
        sysNoticeMapper.insert(dbNotice);

        // 查询
        NoticeDO notice = sysNoticeService.getNotice(dbNotice.getId());

        // 验证插入与读取对象是否一致
        assertNotNull(notice);
        assertPojoEquals(dbNotice, notice);
    }

    @Test
    public void testCreateNotice_success() {
        // 准备参数
        NoticeCreateReqVO reqVO = randomNoticeCreateReqVO();

        // 校验插入是否成功
        Long noticeId = sysNoticeService.createNotice(reqVO);
        assertNotNull(noticeId);

        // 校验插入属性是否正确
        NoticeDO notice = sysNoticeMapper.selectById(noticeId);
        assertPojoEquals(reqVO, notice);
    }

    @Test
    public void testUpdateNotice_success() {
        // 插入前置数据
        NoticeDO dbNoticeDO = randomNoticeDO();
        sysNoticeMapper.insert(dbNoticeDO);

        // 准备更新参数
        NoticeUpdateReqVO reqVO = randomNoticeUpdateReqVO(o -> o.setId(dbNoticeDO.getId()));

        // 更新
        sysNoticeService.updateNotice(reqVO);

        // 检验是否更新成功
        NoticeDO notice = sysNoticeMapper.selectById(reqVO.getId());
        assertPojoEquals(reqVO, notice);
    }

    @Test
    public void testDeleteNotice_success() {
        // 插入前置数据
        NoticeDO dbNotice = randomNoticeDO();
        sysNoticeMapper.insert(dbNotice);

        // 删除
        sysNoticeService.deleteNotice(dbNotice.getId());

        // 检查是否删除成功
        assertNull(sysNoticeMapper.selectById(dbNotice.getId()));
    }

    @Test
    public void checkNoticeExists_success() {
        // 插入前置数据
        NoticeDO dbNotice = randomNoticeDO();
        sysNoticeMapper.insert(dbNotice);

        // 成功调用
        sysNoticeService.checkNoticeExists(dbNotice.getId());
    }

    @Test
    public void checkNoticeExists_noExists() {
        assertServiceException(() -> sysNoticeService.checkNoticeExists(randomLongId()), NOTICE_NOT_FOUND);
    }

    @SafeVarargs
    private static NoticeDO randomNoticeDO(Consumer<NoticeDO>... consumers) {
        NoticeDO notice = randomPojo(NoticeDO.class, consumers);
        notice.setType(randomEle(NoticeTypeEnum.values()).getType());
        notice.setStatus(CommonStatusEnum.ENABLE.getStatus());
        return notice;
    }

    @SafeVarargs
    private static NoticeUpdateReqVO randomNoticeUpdateReqVO(Consumer<NoticeUpdateReqVO>... consumers) {
        NoticeUpdateReqVO reqVO = randomPojo(NoticeUpdateReqVO.class, consumers);
        reqVO.setType(randomEle(NoticeTypeEnum.values()).getType());
        reqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());
        return reqVO;
    }

    private static NoticeCreateReqVO randomNoticeCreateReqVO() {
        NoticeCreateReqVO reqVO = randomPojo(NoticeCreateReqVO.class);
        reqVO.setType(randomEle(NoticeTypeEnum.values()).getType());
        reqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());
        return reqVO;
    }


}
