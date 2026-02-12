package com.finalproject.ecommerce.ecommerce.shared.domain.model.entities;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Date;

@EntityListeners(AuditingEntityListener.class)
@MappedSuperclass
@Data
public class AuditableModel {

    @CreatedDate
    @Column(nullable = true, updatable = false)
    private Date createdAt;

    @LastModifiedDate
    @Column(nullable = true)
    private Date updatedAt;
}