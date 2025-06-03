package com.mogatshoo.dev.point.detail.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mogatshoo.dev.point.detail.entity.PointEntity;

public interface PointRepository extends JpaRepository<PointEntity, String> {

}
