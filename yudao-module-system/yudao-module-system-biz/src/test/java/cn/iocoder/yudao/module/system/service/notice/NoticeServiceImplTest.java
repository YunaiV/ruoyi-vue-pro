package cn.iocoder.yudao.module.system.service.notice;

import cn.iocoder.yudao.framework.common.enums.CommonStatusEnum;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.system.controller.admin.notice.vo.NoticeCreateReqVO;
import cn.iocoder.yudao.module.system.controller.admin.notice.vo.NoticePageReqVO;
import cn.iocoder.yudao.module.system.controller.admin.notice.vo.NoticeUpdateReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.notice.NoticeDO;
import cn.iocoder.yudao.module.system.dal.mysql.notice.NoticeMapper;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import javax.annotation.Resource;

import static cn.iocoder.yudao.framework.common.util.object.ObjectUtils.cloneIgnoreId;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertPojoEquals;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomLongId;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.system.enums.ErrorCodeConstants.NOTICE_NOT_FOUND;
import static org.junit.jupiter.api.Assertions.*;

@Import(NoticeServiceImpl.class)
class NoticeServiceImplTest extends BaseDbUnitTest {

    @Resource
    private NoticeServiceImpl noticeService;

    @Resource
    private NoticeMapper noticeMapper;

    @Test
    public void testGetNoticePage_success() {
        // 插入前置数据
        NoticeDO dbNotice = randomPojo(NoticeDO.class, o -> {
            o.setTitle("尼古拉斯赵四来啦！");
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
        });
        noticeMapper.insert(dbNotice);
        // 测试 title 不匹配
        noticeMapper.insert(cloneIgnoreId(dbNotice, o -> o.setTitle("尼古拉斯凯奇也来啦！")));
        // 测试 status 不匹配
        noticeMapper.insert(cloneIgnoreId(dbNotice, o -> o.setStatus(CommonStatusEnum.DISABLE.getStatus())));
        // 准备参数
        NoticePageReqVO reqVO = new NoticePageReqVO();
        reqVO.setTitle("尼古拉斯赵四来啦！");
        reqVO.setStatus(CommonStatusEnum.ENABLE.getStatus());

        // 调用
        PageResult<NoticeDO> pageResult = noticeService.getNoticePage(reqVO);
        // 验证查询结果经过筛选
        assertEquals(1, pageResult.getTotal());
        assertEquals(1, pageResult.getList().size());
        assertPojoEquals(dbNotice, pageResult.getList().get(0));
    }

    @Test
    public void testGetNotice_success() {
        // 插入前置数据
        NoticeDO dbNotice = randomPojo(NoticeDO.class);
        noticeMapper.insert(dbNotice);

        // 查询
        NoticeDO notice = noticeService.getNotice(dbNotice.getId());

        // 验证插入与读取对象是否一致
        assertNotNull(notice);
        assertPojoEquals(dbNotice, notice);
    }

    @Test
    public void testCreateNotice_success() {
        // 准备参数
        NoticeCreateReqVO reqVO = randomPojo(NoticeCreateReqVO.class);

        // 调用
        Long noticeId = noticeService.createNotice(reqVO);
        // 校验插入属性是否正确
        assertNotNull(noticeId);
        NoticeDO notice = noticeMapper.selectById(noticeId);
        assertPojoEquals(reqVO, notice);
    }

    @Test
    public void testUpdateNotice_success() {
        // 插入前置数据
        NoticeDO dbNoticeDO = randomPojo(NoticeDO.class);
        noticeMapper.insert(dbNoticeDO);

        // 准备更新参数
        NoticeUpdateReqVO reqVO = randomPojo(NoticeUpdateReqVO.class, o -> o.setId(dbNoticeDO.getId()));

        // 更新
        noticeService.updateNotice(reqVO);
        // 检验是否更新成功
        NoticeDO notice = noticeMapper.selectById(reqVO.getId());
        assertPojoEquals(reqVO, notice);
    }

    @Test
    public void testDeleteNotice_success() {
        // 插入前置数据
        NoticeDO dbNotice = randomPojo(NoticeDO.class);
        noticeMapper.insert(dbNotice);

        // 删除
        noticeService.deleteNotice(dbNotice.getId());

        // 检查是否删除成功
        assertNull(noticeMapper.selectById(dbNotice.getId()));
    }

    @Test
    public void testValidateNoticeExists_success() {
        // 插入前置数据
        NoticeDO dbNotice = randomPojo(NoticeDO.class);
        noticeMapper.insert(dbNotice);

        // 成功调用
        noticeService.validateNoticeExists(dbNotice.getId());
    }

    @Test
    public void testValidateNoticeExists_noExists() {
        assertServiceException(() ->
                noticeService.validateNoticeExists(randomLongId()), NOTICE_NOT_FOUND);
    }

}
