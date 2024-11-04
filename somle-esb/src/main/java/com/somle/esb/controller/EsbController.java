package com.somle.esb.controller;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.system.api.dept.DeptApi;
import cn.iocoder.yudao.module.system.controller.admin.dept.vo.dept.DeptSaveReqVO;
import cn.iocoder.yudao.module.system.dal.dataobject.dept.DeptDO;
import cn.iocoder.yudao.module.system.service.dept.DeptService;
import com.somle.eccang.model.EccangCategory;
import com.somle.eccang.model.EccangResponse;
import com.somle.eccang.service.EccangService;
import com.somle.esb.converter.ErpToEccangConverter;
import com.somle.esb.model.Domain;
import com.somle.esb.service.EsbService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

@RestController
@RequestMapping("/api/esb")
public class EsbController {

    @Autowired
    private EsbService service;

    @PostMapping("/getBeans")
    public void printAllBeans() {
        service.printAllBeans();
    }



//    @PostMapping("/dataCollect")
//    public String dataCollect(LocalDate scheduleDate, String database) {
//        if (database == null) {
//            service.dataCollect(scheduleDate);
//        } else {
//            service.dataCollect(scheduleDate, Domain.fromString(database));
//        }
//        return "success";
//    }

    @Resource
    private DeptService deptService;
    @PostMapping("/syncDepartments")
    public String syncDepartments() {
        service.syncEccangDepartments();
        return "success";
    }

    @PostMapping("/syncUsers")
    public String syncUsers() {
        service.syncUsers();
        return "success";
    }


//    @RequestMapping(value = "ip")
//    public JSONObject getIP(HttpServletRequest request, HttpServletResponse response) {
//
//        String ip = request.getRemoteAddr();
//        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
//            ip = request.getHeader("Proxy-Client-IP");
//        }
//        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
//            ip = request.getHeader("WL-Proxy-Client-IP");
//        }
//        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
//            ip = request.getHeader("HTTP_CLIENT_IP");
//        }
//        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
//            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
//        }
//        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
//            ip = request.getRemoteAddr();
//        }
//
//
//        //构建json
//        Map<String, Object> map = new HashMap();
//        map.put("IP", ip);
//        map.put("remote address", request.getRemoteAddr());
//        map.put("remote host", request.getRemoteHost());
//        map.put("remote user", request.getRemoteUser());
//        map.put("remote port", request.getRemotePort());
//        map.put("local address", request.getLocalAddr());
//        map.put("local name", request.getLocalName());
//        map.put("local port", request.getLocalPort());
//        JSONObject jsonObject = new JSONObject(map);
//        return jsonObject;
//    }
}