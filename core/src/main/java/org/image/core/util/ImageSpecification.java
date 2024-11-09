package org.image.core.util;

import org.image.core.repository.entity.ImageEntity;
import org.image.core.repository.entity.UserEntity;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

public class ImageSpecification {

    public static Specification<ImageEntity> hasUserId(UserEntity userEntity) {
        return ((root, query, criteriaBuilder) -> userEntity == null ? null : criteriaBuilder.equal(root.get("userEntity"), userEntity));
    }

    public static Specification<ImageEntity> hasId(Long id) {
        return (root, query, criteriaBuilder) -> id == null ? null : criteriaBuilder.equal(root.get("id"), id);
    }

    public static Specification<ImageEntity> hasDate(LocalDate date) {
        return (root, query, criteriaBuilder) -> {
            if (date == null) {
                return null;
            }
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.atTime(23, 59, 59);

            return criteriaBuilder.between(root.get("uploadDate"), startOfDay, endOfDay);
        };
    }

    public static Specification<ImageEntity> hasSize(Long size) {
        return (root, query, criteriaBuilder) -> size == null ? null : criteriaBuilder.equal(root.get("size"), size);
    }
}
