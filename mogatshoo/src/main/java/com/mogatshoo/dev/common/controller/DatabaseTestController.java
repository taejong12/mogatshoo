package com.mogatshoo.dev.common.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class DatabaseTestController {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @GetMapping("/db-connection")
    public Map<String, Object> testDbConnection() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            String dbVersion = jdbcTemplate.queryForObject("SELECT version()", String.class);
            result.put("success", true);
            result.put("message", "데이터베이스 연결 성공");
            result.put("version", dbVersion);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "데이터베이스 연결 실패: " + e.getMessage());
            e.printStackTrace();
        }
        
        return result;
    }
    
    @GetMapping("/voting-table")
    public Map<String, Object> checkVotingTable() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // voting 테이블 정보 가져오기
            Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'mogatshoo' AND table_name = 'voting'", 
                Integer.class
            );
            
            if (count != null && count > 0) {
                // 테이블이 존재하는 경우 레코드 수 확인
                Integer recordCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM voting", Integer.class);
                result.put("success", true);
                result.put("tableExists", true);
                result.put("recordCount", recordCount);
                result.put("message", "voting 테이블이 존재하며 " + recordCount + "개의 레코드가 있습니다.");
            } else {
                result.put("success", true);
                result.put("tableExists", false);
                result.put("message", "voting 테이블이 존재하지 않습니다.");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "테이블 확인 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
        }
        
        return result;
    }
}