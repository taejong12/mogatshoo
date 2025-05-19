package com.mogatshoo.dev.hospitalMap.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("hospitalMap")
public class HospitalController {

    @Value("${kakao.api.key}")
    private String kakaoApiKey;
    
    @GetMapping("/hospitals")
    public String showHospitalMap(Model model) {
        // 카카오맵 API 키를 뷰로 전달
        model.addAttribute("kakaoApiKey", kakaoApiKey);
        return "map/hospitalMap";
    }
}