package com.somle.dingtalk.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.OapiUserListidRequest;
import com.dingtalk.api.request.OapiV2UserGetRequest;
import com.dingtalk.api.response.OapiUserListidResponse;
import com.dingtalk.api.response.OapiV2UserGetResponse;
import cn.iocoder.yudao.framework.common.util.json.JSONObject;
import cn.iocoder.yudao.framework.common.util.json.JsonUtilsX;
import cn.iocoder.yudao.framework.common.util.web.RequestX;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.annotation.PostConstruct;

import lombok.SneakyThrows;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.integration.support.MessageBuilder;
//import org.springframework.messaging.MessageChannel;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.somle.dingtalk.model.DingTalkDepartment;
// import com.somle.model.DingTalkDepartmentMap.DingTalkDepartment;
// import com.somle.model.DingTalkDepartmentMap;
import com.somle.dingtalk.model.DingTalkResponse;
import com.somle.dingtalk.model.DingTalkToken;
import com.somle.dingtalk.repository.DingTalkTokenRepository;
import cn.iocoder.yudao.framework.common.util.web.WebUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class DingTalkService {
    private DingTalkToken token;
    private final String HOST = "https://api.dingtalk.com";
    private final String BASE_HOST = "https://oapi.dingtalk.com";




//    @Autowired
//    MessageChannel departmentChannel;

    @Autowired
    DingTalkTokenRepository tokenRepository;

    @PostConstruct
    private void init() {
        this.token = tokenRepository.findAll().get(0);
    }


    // public String getAccessToken() {
    //     String url = "https://oapi.dingtalk.com/gettoken?appkey=" + appKey + "&appsecret=" + appSecret;
    //     String response = restTemplate.getForObject(url, String.class);
    //     log.debug(response.toString());

    //     JSONObject jsonObject = JsonUtilsSomle.parseObject(response);
    //     return jsonObject.getString("access_token");
    // }

//    @Scheduled(cron = "0 0 * * * ?") // Executes at the start of every hour
    @Scheduled(initialDelay = 2000, fixedRate = 3600000)
    public void refreshAuth() {
        String url = HOST + "/v1.0/oauth2/accessToken";
        var payload = JsonUtilsX.newObject();
        payload.put("appKey", this.token.getAppKey());
        payload.put("appSecret", this.token.getAppSecret());
        var request = RequestX.builder()
            .requestMethod(RequestX.Method.POST)
            .url(url)
            .payload(payload)
            .build();
        ObjectNode result= WebUtils.sendRequest(request, ObjectNode.class);
        JSONObject jsonObject=new JSONObject(result);
        String accessToken =jsonObject.getString("accessToken");
        this.token.setAccessToken(accessToken);
    }

    @SneakyThrows
    public void sendRobotMessage(String content, String accessToken) {
        var endpoint = "/robot/send";
        var keyword = "ALERT";
        // 构造消息内容
        JSONObject json = new JSONObject();
        json.put("msgtype", "text");

        JSONObject text = new JSONObject();
        text.put("content", keyword + "\n" + content);

        json.put("text", text);

        var queryParams = Map.of("access_token", accessToken);

        RequestX request = RequestX.builder()
            .requestMethod(RequestX.Method.POST)
            .url(BASE_HOST + endpoint)
            .queryParams(queryParams)
            .payload(json)
            .build();

        try (Response response = WebUtils.sendRequest(request)) {
            if (response.isSuccessful()) {
                System.out.println("Response: " + response.body().string());
            } else {
                System.out.println("Failed: " + response.message());
            }
        }
    }


    public DingTalkDepartment getDepartment(Long deptId) {
        log.info("fetching department detail for " + deptId);
        String endUrl = "/topapi/v2/department/get";

        Map<String, String> params = Map.of(
            "access_token", token.getAccessToken()
        );
        Map<String, String> headers = Map.of(
        );
        var payload = JsonUtilsX.newObject();
        payload.put("dept_id", deptId);
        try {
            var request = RequestX.builder()
                .requestMethod(RequestX.Method.POST)
                .url(BASE_HOST + endUrl)
                .queryParams(params)
                .headers(headers)
                .payload(payload)
                .build();
            DingTalkResponse response = WebUtils.sendRequest(request, DingTalkResponse.class);
            // 检查响应
            validateResponse(response);
            // 返回部门详情
            return response.getResult(DingTalkDepartment.class);
        } catch (Exception e) {
            throw new RuntimeException("获取部门id为" + deptId + "信息失败：" + e.getMessage());
        }

//        DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/topapi/v2/department/get");
//        OapiV2DepartmentGetRequest req = new OapiV2DepartmentGetRequest();
//        req.setDeptId(deptId);
//        req.setLanguage("zh_CN");
//        OapiV2DepartmentGetResponse rsp = client.execute(req, token.getAccessToken());
//        return rsp.getResult();

    }
    private void validateResponse(DingTalkResponse response){
        // 检查响应是否为空
        if (response == null) {
            throw new RuntimeException("DingTalk返回空响应");
        }
        if (response.getResult() == null){
            throw new RuntimeException("DingTalk返回空结果");
        }
        if (!Objects.equals(response.getErrcode(),0)){
            throw new RuntimeException("DingTalk返回异常：" + response.getErrmsg());
        }
        log.debug(response.toString());
    }

    public Stream<DingTalkDepartment> getDepartmentPath(Long deptId) {
        DingTalkDepartment current = getDepartment(deptId);
        return Stream.concat(Stream.of(current), getDepartmentPath(current.getParentId()));
    }

    public DingTalkDepartment getParent(Long deptId, Integer parentLevel) {
        List<DingTalkDepartment> path = getDepartmentPath(deptId).toList();
        return path.get(path.size() - 2);
    }

    // public DingTalkDepartmentMap getDepartmentMap(DingTalkDepartmentMap depts, DingTalkDepartment parent) {
    //     getChilds(parent).forEach(child->{
    //         depts.getMap().put(child.getDeptId(), child);
    //     });
    // }

    // public DingTalkDepartmentMap getDepartmentMap() {
    //     DingTalkDepartmentMap depts = new DingTalkDepartmentMap();
    //     return getDepartmentMap(depts, depts.getRoot());
    // }

    //return dept with null level
    public Stream<DingTalkDepartment> getOrphans(Long deptId) {
        log.info("fetching department child for " + deptId);
        String endUrl = "/topapi/v2/department/listsub";

        Map<String, String> params = Map.of(
            "access_token", token.getAccessToken()
        );
        Map<String, String> headers = Map.of(
        );
        var payload = JsonUtilsX.newObject();
        payload.put("dept_id", deptId);
        var request = RequestX.builder()
            .requestMethod(RequestX.Method.POST)
            .url(BASE_HOST + endUrl)
            .queryParams(params)
            .headers(headers)
            .payload(payload)
            .build();
        DingTalkResponse response = WebUtils.sendRequest(request, DingTalkResponse.class);
        log.debug(response.toString());
        return response.getResultList(DingTalkDepartment.class).stream();
    }

    //accept/return dept with non null level
    public Stream<DingTalkDepartment> getChilds(DingTalkDepartment dept) {
        // List<DingTalkDepartment> childPath = dept.getPath();
        // childPath.addLast(dept);
        return getOrphans(dept.getDeptId())
            .map(n->{
                n.setLevel(dept.getLevel() + 1);
                n.setParent(dept);
                return n;
            });
    }

    public DingTalkDepartment cleanDepartment(DingTalkDepartment dept) {
        String name = dept.getName();
        // Map<String, String> replacements = new HashMap<>();
        // replacements.put("创意", "CBD");
        // replacements.put("家居", "HCD");
        // replacements.put("办公", "EBD");
        // replacements.put("视听", "ABD");





        // boolean replaced = false;

        // // Check if the name starts with any key in the map
        // for (String key : replacements.keySet()) {
        //     if (name.startsWith(key)) {
        //         name = replacements.get(key) + name.substring(key.length());
        //         replaced = true;
        //         break;
        //     }
        // }

        // If the name starts with a non-ASCII character and was not replaced
        // char ch = name.charAt(0);
        // if (!replaced && !(ch >= 0 && ch <= 127)) {
        //     name = "$" + name;
        // }
        
        // if (name.startsWith("$") && name.contains("仓")) {
        //     name = "LC" + name.substring(1);
        // } else if (name.startsWith("$")) {
        //     name = "MC" + name.substring(1);
        // }

        dept.setName(name);

        return dept;

    }

    public DingTalkResponse addDepartment(DingTalkDepartment dept) {
        log.info("adding departments");
        String endUrl = "/topapi/v2/department/update";
        
        Map<String, String> params = Map.of(
            "access_token", token.getAccessToken()
        );
        Map<String, String> headers = Map.of(
        );
        var request = RequestX.builder()
            .requestMethod(RequestX.Method.POST)
            .url(BASE_HOST + endUrl)
            .queryParams(params)
            .headers(headers)
            .payload(dept)
            .build();
        DingTalkResponse response = WebUtils.sendRequest(request, DingTalkResponse.class);
        return response;
    }


    public boolean cleanDepartments() {
        log.info("cleaning departments");

        
        return getDepartmentStream().map( dept -> {
            DingTalkDepartment newDept = cleanDepartment(dept);
            DingTalkResponse response = addDepartment(newDept);
            boolean success = response.getErrcode() == 200;
            if (success) {
                log.debug(newDept.getName() + " cleaned successfully");
            } else {
                log.error(newDept.getName() + " clean failed\n" + response.getErrmsg());
            }
            return success;
        })
        .reduce(true, (a, b) -> a && b);
    }
    
    public Stream<DingTalkDepartment> getChildStream(DingTalkDepartment dept) {
        // List<String> keys = List.of("CBD", "HCD", "EBD", "ABD", "DCD", "VC", "QC", "MC", "FC", "HC", "CC", "PC", "LC");
        List<DingTalkDepartment> childList = getChilds(dept).toList();
        if ( childList.isEmpty()) {
            log.debug("null child list");
            return Stream.of();
        } else {
            log.debug("non null child list");
            log.debug(childList.toString());
            return Stream.concat(childList.stream(), childList.stream().flatMap(n -> getChildStream(n)));
            // return Stream.concat(childList.stream().filter(n->! keys.stream().anyMatch(key->n.getName().startsWith(key))), childList.stream().flatMap(n -> getDepartmentsResursive(n)));
        }
    }

    public Stream<DingTalkDepartment> getDepartmentStream() {
        var topDept = getDepartment(1L).setLevel(0);
        return Stream.concat(Stream.of(topDept), getChildStream(topDept));
    }

    




//    public void uploadDepartmentsResursive() {
//        getDepartmentStream()
//            // .limit(4)
//            .forEach(n -> {
//                departmentChannel.send(MessageBuilder.withPayload(n).build());
//                // departmentChannel.send(MessageBuilder.withPayload(n).build());
//            });
//    }

    @SneakyThrows
    public List<String> getUserIds(Long deptId) {
        DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/topapi/user/listid");
        OapiUserListidRequest req = new OapiUserListidRequest();
        req.setDeptId(deptId);
        OapiUserListidResponse rsp = client.execute(req, token.getAccessToken());
        return rsp.getResult().getUseridList();
    }

    @SneakyThrows
    public OapiV2UserGetResponse.UserGetResponse getUserDetail(String userId) {
        DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/topapi/v2/user/get");
        OapiV2UserGetRequest req = new OapiV2UserGetRequest();
        req.setUserid(userId);
        req.setLanguage("zh_CN");
        OapiV2UserGetResponse rsp = client.execute(req, token.getAccessToken());
        return rsp.getResult();
    }

    public Stream<OapiV2UserGetResponse.UserGetResponse> getUserDetailStream() {
        return getDepartmentStream()
            .map(DingTalkDepartment::getDeptId)
            .map(this::getUserIds)
            .flatMap(Collection::stream)
            .map(this::getUserDetail);
    }

    
}
