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
        @RequestParam Integer idSite,
        @RequestParam String date
    ) {
        return matomoService.getVisits(idSite, LocalDate.parse(date)).toList();
    }

    
}
