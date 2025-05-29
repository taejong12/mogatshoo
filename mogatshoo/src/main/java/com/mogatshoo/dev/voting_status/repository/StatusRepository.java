package com.mogatshoo.dev.voting_status.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class StatusRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    //모든 질문의 기본 정보 조회
    public List<Map<String, Object>> getAllQuestionInfo() {
        try {
            // 먼저 테이블이 존재하는지 확인
            String checkTableSql = "SHOW TABLES LIKE 'question'";
            List<Map<String, Object>> tableExists = jdbcTemplate.queryForList(checkTableSql);
            
            if (tableExists.isEmpty()) {
                System.out.println("question 테이블이 존재하지 않습니다.");
                return List.of();
            }
            
            // 컬럼이 존재하는지 확인
            String checkColumnSql = "SHOW COLUMNS FROM question LIKE 'serialNumber'";
            List<Map<String, Object>> columnExists = jdbcTemplate.queryForList(checkColumnSql);
            
            if (columnExists.isEmpty()) {
                System.out.println("question 테이블에 serialNumber 컬럼이 존재하지 않습니다.");
                return List.of();
            }
            
            String sql = """
                SELECT 
                    q.serialNumber,
                    q.question as questionContent,
                    q.isPublic as isEnded,
                    q.createdAt
                FROM question q
                ORDER BY q.createdAt DESC
            """;
            
            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql);
            
            if (results.isEmpty()) {
                System.out.println("question 테이블에 데이터가 없습니다.");
            }
            
            return results;
            
        } catch (Exception e) {
            System.err.println("질문 정보 조회 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            return List.of(); // 빈 리스트 반환
        }
    }

    //특정 질문의 투표 통계 조회
    public Map<String, Object> getQuestionVoteStatistics(String serialNumber) {
        String sql = """
            SELECT 
                v.votedId,
                COUNT(*) as voteCount,
                m.memberName
            FROM voting v
            LEFT JOIN member m ON v.votedId = m.memberId
            WHERE v.serialNumber = ?
            GROUP BY v.votedId, m.memberName
            ORDER BY voteCount DESC
        """;
        
        List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, serialNumber);
        
        Map<String, Object> statistics = new HashMap<>();
        Map<String, Long> voteDetails = new HashMap<>();
        
        if (!results.isEmpty()) {
            // 최다득표자 정보
            Map<String, Object> topResult = results.get(0);
            statistics.put("topVotedId", topResult.get("votedId"));
            statistics.put("topVotedName", topResult.get("memberName"));
            statistics.put("topVoteCount", ((Number) topResult.get("voteCount")).longValue());
            
            // 전체 투표 결과
            long totalVotes = 0;
            for (Map<String, Object> result : results) {
                String votedId = (String) result.get("votedId");
                Long voteCount = ((Number) result.get("voteCount")).longValue();
                voteDetails.put(votedId, voteCount);
                totalVotes += voteCount;
            }
            statistics.put("totalVotes", totalVotes);
        } else {
            statistics.put("topVotedId", null);
            statistics.put("topVotedName", "투표 없음");
            statistics.put("topVoteCount", 0L);
            statistics.put("totalVotes", 0L);
        }
        
        statistics.put("voteDetails", voteDetails);
        return statistics;
    }

    //전체 멤버 수 조회
    public Long getTotalMemberCount() {
        try {
            String sql = "SELECT COUNT(*) FROM member";
            return jdbcTemplate.queryForObject(sql, Long.class);
        } catch (Exception e) {
            System.err.println("멤버 수 조회 오류: " + e.getMessage());
            return 0L; // 기본값 반환
        }
    }

    //특정 질문에 투표한 고유 회원 수 조회
    public Long getUniqueVoterCount(String serialNumber) {
        try {
            String sql = "SELECT COUNT(DISTINCT voterId) FROM voting WHERE serialNumber = ?";
            return jdbcTemplate.queryForObject(sql, Long.class, serialNumber);
        } catch (Exception e) {
            System.err.println("투표자 수 조회 오류: " + e.getMessage());
            return 0L;
        }
    }

    //특정 질문의 상세 정보 조회
    public Map<String, Object> getQuestionDetail(String serialNumber) {
        try {
            String sql = """
                SELECT 
                    serialNumber,
                    question as questionContent,
                    isPublic as isEnded,
                    createdAt
                FROM question
                WHERE serialNumber = ?
            """;
            
            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, serialNumber);
            return results.isEmpty() ? null : results.get(0);
        } catch (Exception e) {
            System.err.println("질문 상세 정보 조회 오류: " + e.getMessage());
            return null;
        }
    }

    //모든 활성 질문의 시리얼 넘버 목록 조회
    public List<String> getAllQuestionSerialNumbers() {
        try {
            String sql = "SELECT serialNumber FROM question ORDER BY createdAt DESC";
            return jdbcTemplate.queryForList(sql, String.class);
        } catch (Exception e) {
            System.err.println("질문 시리얼 넘버 조회 오류: " + e.getMessage());
            return List.of(); // 빈 리스트 반환
        }
    }
}
