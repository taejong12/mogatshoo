package com.mogatshoo.dev.hair_loss_test.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.mogatshoo.dev.hair_loss_test.entity.StageEntity;

@Repository
public interface StageRepository extends JpaRepository<StageEntity, String> {
}
