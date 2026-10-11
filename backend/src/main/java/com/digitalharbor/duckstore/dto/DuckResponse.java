package com.digitalharbor.duckstore.dto;

import com.digitalharbor.duckstore.entity.Duck;
import com.digitalharbor.duckstore.entity.enums.Color;
import com.digitalharbor.duckstore.entity.enums.Size;
import java.math.BigDecimal;

public record DuckResponse(Integer id, Color color, Size size, BigDecimal price, int quantity) {
    public static DuckResponse from(Duck d) {
        return new DuckResponse(d.getId(), d.getColor(), d.getSize(), d.getPrice(), d.getQuantity());
    }
}
