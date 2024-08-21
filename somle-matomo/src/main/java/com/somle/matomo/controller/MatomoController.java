package com.somle.matomo.controller;



import com.somle.matomo.model.MatomoVisit;
import com.somle.matomo.service.MatomoService;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/matomo")
public class MatomoController {
    @Autowired
    private MatomoService matomoService;

    @GetMapping("/getVisits")
    @ResponseBody
    public List<MatomoVisit> getVisits(
        @RequestParam String date
    ) {
        return matomoService.getVisits(5, LocalDate.parse(date)).toList();
    }

    @GetMapping("/dataCollect")
    @ResponseBody
    public void dataCollect(
        @RequestParam String scheduleDate
    ) {
        matomoService.dataCollect(LocalDate.parse(scheduleDate));
    }

    
}
