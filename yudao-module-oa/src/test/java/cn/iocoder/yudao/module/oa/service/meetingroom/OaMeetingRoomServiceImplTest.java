package cn.iocoder.yudao.module.oa.service.meetingroom;

import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.test.core.ut.BaseDbUnitTest;
import cn.iocoder.yudao.module.oa.controller.admin.meetingroom.vo.room.OaMeetingRoomPageReqVO;
import cn.iocoder.yudao.module.oa.controller.admin.meetingroom.vo.room.OaMeetingRoomSaveReqVO;
import cn.iocoder.yudao.module.oa.dal.dataobject.meetingroom.OaMeetingRoomDO;
import cn.iocoder.yudao.module.oa.dal.mysql.meetingroom.OaMeetingRoomMapper;
import cn.iocoder.yudao.module.system.api.dict.DictDataApi;
import cn.iocoder.yudao.module.system.api.user.AdminUserApi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.util.Arrays;
import java.util.Collections;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertList;
import static cn.iocoder.yudao.framework.test.core.util.AssertUtils.assertServiceException;
import static cn.iocoder.yudao.framework.test.core.util.RandomUtils.randomPojo;
import static cn.iocoder.yudao.module.oa.enums.ErrorCodeConstants.MEETING_ROOM_HAS_BOOKING;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * {@link OaMeetingRoomServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import(OaMeetingRoomServiceImpl.class)
public class OaMeetingRoomServiceImplTest extends BaseDbUnitTest {

    @Resource
    private OaMeetingRoomService meetingRoomService;

    @Resource
    private OaMeetingRoomMapper meetingRoomMapper;
    @Resource
    private DataSource dataSource;

    @MockBean
    private OaMeetingRoomBookingService meetingRoomBookingService;
    @MockBean
    private AdminUserApi adminUserApi;
    @MockBean
    private DictDataApi dictDataApi;

    @BeforeEach
    public void before() {
        // H2 不支持 JSON_CONTAINS，补充数字数组成员匹配函数用于查询测试
        new JdbcTemplate(dataSource).execute("CREATE ALIAS IF NOT EXISTS JSON_CONTAINS FOR "
                + "\"cn.iocoder.yudao.module.oa.service.meetingroom.OaMeetingRoomServiceImplTest.jsonContains\"");
    }

    /**
     * 为 H2 查询提供 MySQL JSON_CONTAINS 的数字数组匹配能力。
     *
     * @param json JSON 数组
     * @param candidate 待匹配的编号
     * @return 数组是否包含编号；空数组字段返回 null
     */
    public static Boolean jsonContains(String json, String candidate) {
        return json == null ? null : JSONUtil.toList(json, Long.class).contains(Long.valueOf(candidate));
    }

    @Test
    public void testUpdateMeetingRoom_success() {
        // mock 数据
        OaMeetingRoomDO room = randomMeetingRoomDO();
        meetingRoomMapper.insert(room);
        // 准备参数
        OaMeetingRoomSaveReqVO reqVO = BeanUtils.toBean(room, OaMeetingRoomSaveReqVO.class);
        reqVO.setName("修改后的会议室");

        // 调用
        meetingRoomService.updateMeetingRoom(reqVO);

        // 断言
        assertEquals(reqVO.getName(), meetingRoomMapper.selectById(room.getId()).getName());
        verify(adminUserApi).validateUser(room.getManagerUserId());
    }

    @Test
    public void testDeleteMeetingRoom_success() {
        // mock 数据
        OaMeetingRoomDO room = randomMeetingRoomDO();
        meetingRoomMapper.insert(room);

        // 调用
        meetingRoomService.deleteMeetingRoom(room.getId());

        // 断言
        assertNull(meetingRoomMapper.selectById(room.getId()));
        verify(meetingRoomBookingService).hasActiveMeetingRoomBooking(room.getId());
    }

    @Test
    public void testDeleteMeetingRoom_hasBooking() {
        // mock 数据
        OaMeetingRoomDO room = randomMeetingRoomDO();
        meetingRoomMapper.insert(room);
        // mock 方法
        when(meetingRoomBookingService.hasActiveMeetingRoomBooking(room.getId())).thenReturn(true);

        // 调用，并断言异常
        assertServiceException(() -> meetingRoomService.deleteMeetingRoom(room.getId()), MEETING_ROOM_HAS_BOOKING);
        assertNotNull(meetingRoomMapper.selectById(room.getId()));
    }

    @Test
    public void testGetMeetingRoomPageByBookingUserId_sortAndPage() {
        // mock 数据
        OaMeetingRoomDO firstRoom = randomMeetingRoomDO().setId(1024L).setSort(1);
        meetingRoomMapper.insert(firstRoom);
        OaMeetingRoomDO secondRoom = randomMeetingRoomDO().setId(1025L).setSort(1);
        meetingRoomMapper.insert(secondRoom);
        OaMeetingRoomDO thirdRoom = randomMeetingRoomDO().setId(1026L).setSort(2);
        meetingRoomMapper.insert(thirdRoom);

        // 准备参数
        OaMeetingRoomPageReqVO reqVO = new OaMeetingRoomPageReqVO();
        reqVO.setPageSize(2);

        // 调用
        PageResult<OaMeetingRoomDO> firstPage = meetingRoomService.getMeetingRoomPageByBookingUserId(reqVO, 1L);
        reqVO.setPageNo(2);
        PageResult<OaMeetingRoomDO> secondPage = meetingRoomService.getMeetingRoomPageByBookingUserId(reqVO, 1L);

        // 断言
        assertEquals(3L, firstPage.getTotal());
        assertEquals(Arrays.asList(1025L, 1024L), convertList(firstPage.getList(), OaMeetingRoomDO::getId));
        assertEquals(3L, secondPage.getTotal());
        assertEquals(Collections.singletonList(1026L), convertList(secondPage.getList(), OaMeetingRoomDO::getId));
    }

    @Test
    public void testGetMeetingRoomPageByBookingUserId_scope() {
        // mock 数据
        OaMeetingRoomDO allRoom = randomMeetingRoomDO().setId(1024L);
        meetingRoomMapper.insert(allRoom);
        OaMeetingRoomDO specifiedRoom = randomMeetingRoomDO().setId(1025L).setBookingScope(1)
                .setBookingUserIds(Arrays.asList(1L, 2L));
        meetingRoomMapper.insert(specifiedRoom);
        meetingRoomMapper.insert(randomMeetingRoomDO().setBookingScope(1).setBookingUserIds(Collections.singletonList(11L)));
        meetingRoomMapper.insert(randomMeetingRoomDO().setBookingScope(1));
        meetingRoomMapper.insert(randomMeetingRoomDO().setStatus(2));
        meetingRoomMapper.insert(randomMeetingRoomDO().setAllowBooking(false));
        // 准备参数
        OaMeetingRoomPageReqVO reqVO = new OaMeetingRoomPageReqVO();
        reqVO.setPageSize(1);

        // 调用
        PageResult<OaMeetingRoomDO> firstPage = meetingRoomService.getMeetingRoomPageByBookingUserId(reqVO, 1L);
        reqVO.setPageNo(2);
        PageResult<OaMeetingRoomDO> secondPage = meetingRoomService.getMeetingRoomPageByBookingUserId(reqVO, 1L);
        reqVO.setPageNo(3);
        PageResult<OaMeetingRoomDO> emptyPage = meetingRoomService.getMeetingRoomPageByBookingUserId(reqVO, 1L);

        // 断言
        assertEquals(2L, firstPage.getTotal());
        assertEquals(Collections.singletonList(1025L), convertList(firstPage.getList(), OaMeetingRoomDO::getId));
        assertEquals(2L, secondPage.getTotal());
        assertEquals(Collections.singletonList(1024L), convertList(secondPage.getList(), OaMeetingRoomDO::getId));
        assertTrue(emptyPage.getList().isEmpty());
        assertEquals(2L, emptyPage.getTotal());
        assertEquals(6L, meetingRoomService.getMeetingRoomPage(new OaMeetingRoomPageReqVO()).getTotal());
    }

    @Test
    public void testGetMeetingRoomPageByBookingUserId_filter() {
        // mock 数据
        OaMeetingRoomDO room = randomMeetingRoomDO().setName("项目会议室").setLocation("三楼");
        meetingRoomMapper.insert(room);
        meetingRoomMapper.insert(randomMeetingRoomDO().setName("培训会议室").setLocation("三楼"));
        meetingRoomMapper.insert(randomMeetingRoomDO().setName("项目会议室").setLocation("二楼"));
        meetingRoomMapper.insert(randomMeetingRoomDO().setName("项目会议室").setLocation("三楼").setType(2));
        // 准备参数
        OaMeetingRoomPageReqVO reqVO = new OaMeetingRoomPageReqVO();
        reqVO.setName("项目").setLocation("三楼").setType(1).setStatus(0);

        // 调用
        PageResult<OaMeetingRoomDO> page = meetingRoomService.getMeetingRoomPageByBookingUserId(reqVO, 1L);

        // 断言
        assertEquals(1L, page.getTotal());
        assertEquals(Collections.singletonList(room.getId()), convertList(page.getList(), OaMeetingRoomDO::getId));
    }

    // ========== 随机对象 ==========

    /**
     * 构造测试数据，固定业务字段的合法取值。
     *
     * @return 未入库的测试对象
     */
    private static OaMeetingRoomDO randomMeetingRoomDO() {
        return randomPojo(OaMeetingRoomDO.class, room -> room.setType(1).setStatus(0)
                .setSeatCount(10).setSort(0).setAllowBooking(true).setNeedApproval(false).setBookingScope(0)
                .setEquipments(Collections.emptyList()).setBookingUserIds(Collections.emptyList())
                .setFileUrls(Collections.emptyList()));
    }

}
