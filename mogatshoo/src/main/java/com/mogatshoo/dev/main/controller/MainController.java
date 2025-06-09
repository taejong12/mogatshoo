package com.mogatshoo.dev.main.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

	@GetMapping("/")
	public String mainPage() {
		return "main/main";
	}

	@GetMapping("/intro")
	public String introVideoPage() {
		return "main/intro";
	}
}