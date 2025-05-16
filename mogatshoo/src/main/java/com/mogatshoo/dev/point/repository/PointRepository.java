package com.mogatshoo.dev.point.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mogatshoo.dev.point.entity.PointEntity;

public interface PointRepository extends JpaRepository<PointEntity, String> {

}
