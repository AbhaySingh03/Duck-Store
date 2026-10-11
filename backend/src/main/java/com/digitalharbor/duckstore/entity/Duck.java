package com.digitalharbor.duckstore.entity;

import com.digitalharbor.duckstore.entity.enums.Color;
import com.digitalharbor.duckstore.entity.enums.Size;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "duck", uniqueConstraints = @UniqueConstraint(
        name = "uq_duck_color_size_price", columnNames = {"color", "duck_size", "price"}))
public class Duck {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private Color color;

    @Enumerated(EnumType.STRING)
    @Column(name = "duck_size", nullable = false, length = 16)
    private Size size;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private boolean deleted;

    protected Duck() {
    }

    public Duck(Color color, Size size, BigDecimal price, int quantity) {
        this.color = color;
        this.size = size;
        this.price = price;
        this.quantity = quantity;
    }

    public Integer getId() { return id; }
    public Color getColor() { return color; }
    public Size getSize() { return size; }
    public BigDecimal getPrice() { return price; }
    public int getQuantity() { return quantity; }
    public boolean isDeleted() { return deleted; }

    public void setPrice(BigDecimal price) { this.price = price; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public void setDeleted(boolean deleted) { this.deleted = deleted; }
}
