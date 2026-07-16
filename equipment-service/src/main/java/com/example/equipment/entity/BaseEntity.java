package com.example.equipment.entity;

import com.example.equipment.utils.ksuuid.KsuidVersion;
import com.example.equipment.utils.ksuuid.KsuidVersionType;
import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.Type;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

@Getter
@MappedSuperclass
public class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDate createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDate updatedAt;

    @Version
    @Type(KsuidVersionType.class)
    @Column(name = "etag")
    private KsuidVersion etag;

    @PrePersist
    protected void onCreate() {
        LocalDate now = LocalDate.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDate.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BaseEntity account = (BaseEntity) o;
        return Objects.equals(id, account.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
