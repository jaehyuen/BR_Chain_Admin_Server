package com.brchain.common.entity;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.Instant;
import java.time.LocalDateTime;

@Data

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    @CreatedDate
    @Column(name ="CREATE_AT", nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name ="MODIFIED_AT", nullable = false)
    private LocalDateTime modifiedAt;
}
