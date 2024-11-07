package org.image.core.util;

import org.image.core.repository.entity.ImageEntity;
import org.image.core.repository.entity.UserEntity;
import org.springframework.data.jpa.domain.Specification;

import java.util.Date;

public class ImageSpecification {

    public static Specification<ImageEntity> hasUserId(UserEntity userEntity) {
        return ((root, query, criteriaBuilder) -> userEntity == null ? null : criteriaBuilder.equal(root.get("userEntity"), userEntity));
    }

    public static Specification<ImageEntity> hasId(Long id) {
        return (root, query, criteriaBuilder) -> id == null ? null : criteriaBuilder.equal(root.get("id"), id);
    }

    public static Specification<ImageEntity> hasDate(Date date) {
        return (root, query, criteriaBuilder) -> date == null ? null : criteriaBuilder.equal(root.get("date"), date);
    }

    public static Specification<ImageEntity> hasSize(Long size) {
        return (root, query, criteriaBuilder) -> size == null ? null : criteriaBuilder.equal(root.get("size"), size);
    }
}
