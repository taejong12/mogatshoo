package com.mogatshoo.dev.admin.voting_status.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class StatusRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 모든 질문의 기본 정보 조회 (active + completed 통합)
     */
    public List<Map<String, Object>> getAllQuestionInfo() {
        try {
            String sql = """
                (SELECT 
                    q.serial_number as serialNumber,
                    q.question as questionContent,
                    q.is_public as isEnded,
                    q.created_at as createdAt,
                    q.voting_start_date as votingStartDate,
                    q.voting_end_date as votingEndDate,
                    'active' as tableSource
                FROM question q)
                UNION ALL
                (SELECT 
                    cq.serial_number as serialNumber,
                    cq.question as questionContent,
                    cq.is_public as isEnded,
                    cq.created_at as createdAt,
                    cq.voting_start_date as votingStartDate,
                    cq.voting_end_date as votingEndDate,
                    'completed' as tableSource
                FROM completed_question cq)
                ORDER BY createdAt DESC
            """;
            
            return jdbcTemplate.queryForList(sql);
            
        } catch (Exception e) {
            System.err.println("질문 정보 조회 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }

    /**
     * 특정 질문의 투표 통계 조회 (voting 테이블은 그대로)
     */
    public Map<String, Object> getQuestionVoteStatistics(String serialNumber) {
        Map<String, Object> statistics = new HashMap<>();
        
        try {
            // 각 후보별 득표수와 후보 이름 조회
            String voteSql = """
                SELECT 
                    v.voted_id as votedId,
                    COUNT(*) as voteCount,
                    COALESCE(m.member_name, v.voted_id) as memberName
                FROM voting v
                LEFT JOIN member m ON v.voted_id = m.member_id
                WHERE v.serial_number = ?
                GROUP BY v.voted_id, m.member_name
                ORDER BY voteCount DESC
            """;
            
            List<Map<String, Object>> voteResults = jdbcTemplate.queryForList(voteSql, serialNumber);
            
            // 총 투표수 계산
            String totalVotesSql = """
                SELECT COUNT(*) as totalVotes
                FROM voting 
                WHERE serial_number = ?
            """;
            Long totalVotes = jdbcTemplate.queryForObject(totalVotesSql, Long.class, serialNumber);
            
            // 고유 투표자 수 계산
            String uniqueVotersSql = """
                SELECT COUNT(DISTINCT voter_id) as uniqueVoters
                FROM voting 
                WHERE serial_number = ?
            """;
            Long uniqueVoters = jdbcTemplate.queryForObject(uniqueVotersSql, Long.class, serialNumber);
            
            // 결과 설정
            Map<String, Long> voteDetails = new HashMap<>();
            
            if (!voteResults.isEmpty()) {
                // 최다득표자 정보
                Map<String, Object> topResult = voteResults.get(0);
                statistics.put("topVotedId", topResult.get("votedId"));
                statistics.put("topVotedName", topResult.get("memberName"));
                statistics.put("topVoteCount", ((Number) topResult.get("voteCount")).longValue());
                
                // 모든 후보의 득표 결과
                for (Map<String, Object> result : voteResults) {
                    String votedId = (String) result.get("votedId");
                    Long voteCount = ((Number) result.get("voteCount")).longValue();
                    voteDetails.put(votedId, voteCount);
                }
            } else {
                statistics.put("topVotedId", null);
                statistics.put("topVotedName", "투표 없음");
                statistics.put("topVoteCount", 0L);
            }
            
            statistics.put("totalVotes", totalVotes != null ? totalVotes : 0L);
            statistics.put("uniqueVoters", uniqueVoters != null ? uniqueVoters : 0L);
            statistics.put("voteDetails", voteDetails);
            
            return statistics;
            
        } catch (Exception e) {
            System.err.println("투표 통계 조회 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            
            // 기본값 반환
            statistics.put("topVotedId", null);
            statistics.put("topVotedName", "오류 발생");
            statistics.put("topVoteCount", 0L);
            statistics.put("totalVotes", 0L);
            statistics.put("uniqueVoters", 0L);
            statistics.put("voteDetails", new HashMap<String, Long>());
            
            return statistics;
        }
    }

    /**
     * USER 권한 멤버 수만 조회
     */
    public Long getTotalMemberCount() {
        try {
            String sql = "SELECT COUNT(*) FROM member WHERE role = 'USER'";
            return jdbcTemplate.queryForObject(sql, Long.class);
        } catch (Exception e) {
            System.err.println("USER 멤버 수 조회 오류: " + e.getMessage());
            return 0L;
        }
    }

    /**
     * 특정 질문의 상세 정보 조회 (두 테이블 모두 확인)
     */
    public Map<String, Object> getQuestionDetail(String serialNumber) {
        try {
            // 먼저 active 테이블에서 조회
            String activeSql = """
                SELECT 
                    serial_number as serialNumber,
                    question as questionContent,
                    is_public as isEnded,
                    created_at as createdAt,
                    voting_start_date as votingStartDate,
                    voting_end_date as votingEndDate,
                    'active' as tableSource
                FROM question
                WHERE serial_number = ?
            """;
            
            List<Map<String, Object>> activeResults = jdbcTemplate.queryForList(activeSql, serialNumber);
            
            if (!activeResults.isEmpty()) {
                return activeResults.get(0);
            }
            
            // active에 없으면 completed 테이블에서 조회
            String completedSql = """
                SELECT 
                    serial_number as serialNumber,
                    question as questionContent,
                    is_public as isEnded,
                    created_at as createdAt,
                    voting_start_date as votingStartDate,
                    voting_end_date as votingEndDate,
                    'completed' as tableSource
                FROM completed_question
                WHERE serial_number = ?
            """;
            
            List<Map<String, Object>> completedResults = jdbcTemplate.queryForList(completedSql, serialNumber);
            return completedResults.isEmpty() ? null : completedResults.get(0);
            
        } catch (Exception e) {
            System.err.println("질문 상세 정보 조회 오류: " + e.getMessage());
            return null;
        }
    }

    /**
     * 검색 기능 - 두 테이블 통합
     */
    public List<Map<String, Object>> searchQuestionInfo(String keyword, String publicStatus, String completionStatus) {
        try {
            StringBuilder sql = new StringBuilder();
            
            // Base query for active questions
            sql.append("(SELECT ");
            sql.append("q.serial_number as serialNumber, ");
            sql.append("q.question as questionContent, ");
            sql.append("q.is_public as isEnded, ");
            sql.append("q.created_at as createdAt, ");
            sql.append("q.voting_start_date as votingStartDate, ");
            sql.append("q.voting_end_date as votingEndDate, ");
            sql.append("'active' as tableSource ");
            sql.append("FROM question q WHERE 1=1 ");
            
            // Add filters for active questions
            if (keyword != null && !keyword.trim().isEmpty()) {
                sql.append("AND (LOWER(q.serial_number) LIKE LOWER(?) OR LOWER(q.question) LIKE LOWER(?)) ");
            }
            if (publicStatus != null && !publicStatus.trim().isEmpty()) {
                sql.append("AND q.is_public = ? ");
            }
            if ("active".equals(completionStatus)) {
                // Only include active questions
            } else if ("completed".equals(completionStatus)) {
                sql.append("AND 1=0 "); // Exclude active questions
            }
            
            sql.append(") UNION ALL ");
            
            // Base query for completed questions
            sql.append("(SELECT ");
            sql.append("cq.serial_number as serialNumber, ");
            sql.append("cq.question as questionContent, ");
            sql.append("cq.is_public as isEnded, ");
            sql.append("cq.created_at as createdAt, ");
            sql.append("cq.voting_start_date as votingStartDate, ");
            sql.append("cq.voting_end_date as votingEndDate, ");
            sql.append("'completed' as tableSource ");
            sql.append("FROM completed_question cq WHERE 1=1 ");
            
            // Add filters for completed questions
            if (keyword != null && !keyword.trim().isEmpty()) {
                sql.append("AND (LOWER(cq.serial_number) LIKE LOWER(?) OR LOWER(cq.question) LIKE LOWER(?)) ");
            }
            if (publicStatus != null && !publicStatus.trim().isEmpty()) {
                sql.append("AND cq.is_public = ? ");
            }
            if ("active".equals(completionStatus)) {
                sql.append("AND 1=0 "); // Exclude completed questions
            } else if ("completed".equals(completionStatus)) {
                // Only include completed questions
            }
            
            sql.append(") ORDER BY createdAt DESC");
            
            // Prepare parameters
            List<Object> params = new ArrayList<>();
            
            // Parameters for active query
            if (keyword != null && !keyword.trim().isEmpty()) {
                params.add("%" + keyword + "%");
                params.add("%" + keyword + "%");
            }
            if (publicStatus != null && !publicStatus.trim().isEmpty()) {
                params.add(publicStatus);
            }
            
            // Parameters for completed query (same as active)
            if (keyword != null && !keyword.trim().isEmpty()) {
                params.add("%" + keyword + "%");
                params.add("%" + keyword + "%");
            }
            if (publicStatus != null && !publicStatus.trim().isEmpty()) {
                params.add(publicStatus);
            }
            
            return jdbcTemplate.queryForList(sql.toString(), params.toArray());
            
        } catch (Exception e) {
            System.err.println("질문 검색 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }
}