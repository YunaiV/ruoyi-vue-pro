package com.somle.microsoft.controller;


import java.time.LocalDate;
import java.util.List;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import com.somle.microsoft.model.PowerbiReportReqVO;
import com.somle.microsoft.model.PowerbiReportRespVO;
import com.somle.microsoft.service.MicrosoftService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/admin-api/microsoft")
public class MicrosoftController {
    @Autowired
    private MicrosoftService service;



    @GetMapping("/getEmbedReport")
    @ResponseBody
    public CommonResult<PowerbiReportRespVO> getEmbedReport(PowerbiReportReqVO param) {
        return CommonResult.success(service.getEmbedReport(param));
    }

}