package cn.iocoder.yudao.module.oa.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * OA 流程初始化测试类
 *
 * 用于手动初始化 OA 业务所需的 BPM 流程模型，不用于业务流程提交或审批测试。
 * 读取 main/resources/bpm 中的流程文件，通过 HTTP 接口导入并发布。
 * 已存在的模型不覆盖，未发布时补充发布，已发布时不重复创建流程版本。
 *
 * @author 芋道源码
 */
@Disabled("手动初始化流程模型时临时移除，需启动本地后端并开启 mock 登录")
@Slf4j
public class OaBpmModelInitTest {

    private static final String BASE_URL = "http://127.0.0.1:48080/admin-api";
    private static final Long TENANT_ID = 1L;
    private static final Long USER_ID = 1L;

    private static final String TOKEN = "test" + USER_ID; // 本地默认 mockSecret 为 test，后缀为模拟用户编号，不是真实登录令牌

    /**
     * 初始化用车申请流程
     */
    @Test
    public void testInitVehicleApplyModel() {
        // 调用，并断言流程已发布
        initModel("oa_vehicle_apply.json");
    }

    /**
     * 初始化还车申请流程
     */
    @Test
    public void testInitVehicleReturnModel() {
        // 调用，并断言流程已发布
        initModel("oa_vehicle_return.json");
    }

    /**
     * 初始化会议室预定流程
     */
    @Test
    public void testInitMeetingRoomBookingModel() {
        // 调用，并断言流程已发布
        initModel("oa_meeting_room_booking.json");
    }

    /**
     * 初始化费用报销流程
     */
    @Test
    public void testInitReimbursementModel() {
        // 调用，并断言流程已发布
        initModel("oa_reimbursement.json");
    }

    /**
     * 初始化加班申请流程
     */
    @Test
    public void testInitOvertimeApplyModel() {
        // 调用，并断言流程已发布
        initModel("oa_overtime_apply.json");
    }

    /**
     * 初始化请假申请流程
     */
    @Test
    public void testInitLeaveApplyModel() {
        // 调用，并断言流程已发布
        initModel("oa_leave_apply.json");
    }

    /**
     * 初始化转正申请流程
     */
    @Test
    public void testInitRegularApplyModel() {
        // 调用，并断言流程已发布
        initModel("oa_regular_apply.json");
    }

    /**
     * 初始化离职申请流程
     */
    @Test
    public void testInitResignApplyModel() {
        // 调用，并断言流程已发布
        initModel("oa_resign_apply.json");
    }

    /**
     * 初始化用品领用申请流程
     */
    @Test
    public void testInitSupplyApplyModel() {
        // 调用，并断言流程已发布
        initModel("oa_supply_apply.json");
    }

    /**
     * 初始化出差申请流程
     */
    @Test
    public void testInitTravelApplyModel() {
        // 调用，并断言流程已发布
        initModel("oa_travel_apply.json");
    }

    /**
     * 初始化出差报销流程
     */
    @Test
    public void testInitTravelReimbursementModel() {
        // 调用，并断言流程已发布
        initModel("oa_travel_reimbursement.json");
    }

    /**
     * 初始化用印申请流程
     */
    @Test
    public void testInitSealApplyModel() {
        // 调用，并断言流程已发布
        initModel("oa_seal_apply.json");
    }

    /**
     * 初始化公文发文流程
     */
    @Test
    public void testInitOfficialDocSendModel() {
        // 调用，并断言流程已发布
        initModel("oa_official_doc_send.json");
    }

    /**
     * 初始化公文收文流程
     */
    @Test
    public void testInitOfficialDocReceiveModel() {
        // 调用，并断言流程已发布
        initModel("oa_official_doc_receive.json");
    }

    /**
     * 初始化流程模型
     *
     * @param fileName bpm 资源目录下的流程文件名
     */
    private void initModel(String fileName) {
        try {
            // 1. 准备模型参数
            JSONObject model = JSONUtil.parseObj(ResourceUtil.readUtf8Str("bpm/" + fileName));
            model.set("managerUserIds", Collections.singletonList(USER_ID));

            // 2. 查询已有模型，不覆盖本地已调整的流程
            JSONObject existingModel;
            try (HttpResponse response = HttpUtil.createGet(BASE_URL + "/bpm/model/list")
                    .header("Authorization", "Bearer " + TOKEN)
                    .header("tenant-id", TENANT_ID.toString())
                    .timeout(30000).execute()) {
                assertEquals(200, response.getStatus());
                JSONObject result = JSONUtil.parseObj(response.body());
                assertEquals(Integer.valueOf(0), result.getInt("code"), result.getStr("msg"));
                existingModel = CollUtil.findOne(result.getJSONArray("data").toList(JSONObject.class),
                        item -> model.getStr("key").equals(item.getStr("key")));
            }

            // 3. 导入不存在的模型，补充发布尚未发布的模型
            String modelId = existingModel != null ? existingModel.getStr("id") : importModel(fileName, model);
            if (existingModel == null || existingModel.getJSONObject("processDefinition") == null) {
                deployModel(modelId);
            }

            // 4. 断言流程定义已生效，可用于业务提交
            try (HttpResponse response = HttpUtil.createGet(BASE_URL + "/bpm/process-definition/get")
                    .header("Authorization", "Bearer " + TOKEN)
                    .header("tenant-id", TENANT_ID.toString())
                    .form("key", model.getStr("key"))
                    .timeout(30000).execute()) {
                assertEquals(200, response.getStatus());
                JSONObject result = JSONUtil.parseObj(response.body());
                assertEquals(Integer.valueOf(0), result.getInt("code"), result.getStr("msg"));
                JSONObject definition = result.getJSONObject("data");
                assertNotNull(definition, "未找到生效的流程定义，请检查模型是否已发布或被挂起");
                assertEquals(model.getStr("key"), definition.getStr("key"));
                assertNotNull(definition.getStr("id"));
                log.info("[initModel][流程初始化成功，文件：{}，模型编号：{}，流程定义编号：{}]",
                        fileName, modelId, definition.getStr("id"));
            }
        } catch (RuntimeException | AssertionError ex) {
            log.error("[initModel][流程初始化失败，文件：{}]", fileName, ex);
            throw ex;
        }
    }

    /**
     * 导入流程模型
     *
     * @param fileName 流程文件名
     * @param model 流程模型
     * @return 模型编号
     */
    private String importModel(String fileName, JSONObject model) {
        try (HttpResponse response = HttpUtil.createPost(BASE_URL + "/bpm/model/import")
                .header("Authorization", "Bearer " + TOKEN)
                .header("tenant-id", TENANT_ID.toString())
                .form("file", model.toString().getBytes(StandardCharsets.UTF_8), fileName)
                .timeout(30000).execute()) {
            assertEquals(200, response.getStatus());
            JSONObject result = JSONUtil.parseObj(response.body());
            assertEquals(Integer.valueOf(0), result.getInt("code"), result.getStr("msg"));
            assertNotNull(result.getStr("data"));
            return result.getStr("data");
        }
    }

    /**
     * 发布流程模型
     *
     * @param modelId 模型编号
     */
    private void deployModel(String modelId) {
        try (HttpResponse response = HttpUtil.createPost(BASE_URL + "/bpm/model/deploy")
                .header("Authorization", "Bearer " + TOKEN)
                .header("tenant-id", TENANT_ID.toString())
                .form("id", modelId)
                .timeout(30000).execute()) {
            assertEquals(200, response.getStatus());
            JSONObject result = JSONUtil.parseObj(response.body());
            assertEquals(Integer.valueOf(0), result.getInt("code"), result.getStr("msg"));
            assertEquals(Boolean.TRUE, result.getBool("data"));
        }
    }
}
