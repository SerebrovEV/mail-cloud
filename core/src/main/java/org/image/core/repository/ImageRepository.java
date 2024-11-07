package org.image.core.repository;

import org.image.core.repository.entity.ImageEntity;
import org.image.core.repository.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ImageRepository extends JpaRepository<ImageEntity, Long>, JpaSpecificationExecutor<ImageEntity> {
   Optional<ImageEntity> findByIdAndUserEntity (Long imageEntityId, UserEntity userEntity);
}