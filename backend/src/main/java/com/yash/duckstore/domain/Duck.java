package com.yash.duckstore.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Entity
@Table(name = "ducks")
public class Duck {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private DuckColor color;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private DuckSize size;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private boolean deleted = false;

    @Version
    @Column(nullable = false)
    private long version;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected Duck() {
    }

    public Duck(DuckColor color, DuckSize size, BigDecimal price, Integer quantity) {
        this.color = color;
        this.size = size;
        this.price = price;
        this.quantity = quantity;
        this.deleted = false;
    }

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now(ZoneOffset.UTC);
    }

    public Long getId() { return id; }
    public DuckColor getColor() { return color; }
    public DuckSize getSize() { return size; }
    public BigDecimal getPrice() { return price; }
    public Integer getQuantity() { return quantity; }
    public boolean isDeleted() { return deleted; }
    public long getVersion() { return version; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void updatePriceAndQuantity(BigDecimal price, Integer quantity) {
        this.price = price;
        this.quantity = quantity;
    }

    public void addQuantity(int amount) {
        this.quantity = Math.addExact(this.quantity, amount);
    }

    public void markDeleted() {
        this.deleted = true;
    }
}
