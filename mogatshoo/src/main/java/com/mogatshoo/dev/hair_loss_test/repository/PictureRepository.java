package com.mogatshoo.dev.hair_loss_test.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.mogatshoo.dev.hair_loss_test.entity.PictureEntity;

@Repository
public interface PictureRepository extends JpaRepository<PictureEntity, String> {
}
