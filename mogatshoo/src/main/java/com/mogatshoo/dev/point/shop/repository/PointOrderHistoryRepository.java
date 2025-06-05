package com.mogatshoo.dev.point.shop.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.mogatshoo.dev.point.shop.entity.PointOrderHistoryEntity;

public interface PointOrderHistoryRepository extends JpaRepository<PointOrderHistoryEntity, Long> {

	Page<PointOrderHistoryEntity> findByMemberId(String memberId, Pageable pageable);

}
