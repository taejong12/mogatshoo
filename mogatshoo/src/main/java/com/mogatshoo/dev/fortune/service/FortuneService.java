package com.mogatshoo.dev.fortune.service;

import java.time.LocalDate;
import java.util.Map;

public interface FortuneService {

	Map<String, Object> fortuneMsg(String name, LocalDate birth, String gender);

}
