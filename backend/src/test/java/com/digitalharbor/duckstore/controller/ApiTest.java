package com.digitalharbor.duckstore.controller;

import com.digitalharbor.duckstore.repository.DuckRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** HTTP-level tests: status codes, validation and the JSON contract. */
@SpringBootTest
@AutoConfigureMockMvc
class ApiTest {

    @Autowired MockMvc mvc;
    @Autowired DuckRepository repository;

    @BeforeEach
    void clean() {
        repository.deleteAll();
    }

    private ResultActions postJson(String url, String json) throws Exception {
        return mvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(json));
    }

    private ResultActions addDuck(String color, String size, String price, int qty) throws Exception {
        return postJson("/api/ducks", String.format(
                "{\"color\":\"%s\",\"size\":\"%s\",\"price\":%s,\"quantity\":%d}", color, size, price, qty));
    }

    private ResultActions order(String color, String size, int qty, String country, String mode) throws Exception {
        return postJson("/api/orders", String.format(
                "{\"color\":\"%s\",\"size\":\"%s\",\"quantity\":%d,\"country\":\"%s\",\"shippingMode\":\"%s\"}",
                color, size, qty, country, mode));
    }

    // ---------- warehouse ----------

    @Test
    void add_returns_201_then_200_when_merged() throws Exception {
        addDuck("Red", "XLarge", "200", 10).andExpect(status().isCreated())
                .andExpect(jsonPath("$.color").value("Red")).andExpect(jsonPath("$.size").value("XLarge"));
        addDuck("Red", "XLarge", "200", 5).andExpect(status().isOk()).andExpect(jsonPath("$.quantity").value(15));
        mvc.perform(get("/api/ducks")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void add_validates_input() throws Exception {
        addDuck("Pink", "XLarge", "200", 1).andExpect(status().isBadRequest());
        addDuck("Red", "Huge", "200", 1).andExpect(status().isBadRequest());
        addDuck("Red", "Large", "0", 1).andExpect(status().isBadRequest());
        addDuck("Red", "Large", "-3", 1).andExpect(status().isBadRequest());
        addDuck("Red", "Large", "1.234", 1).andExpect(status().isBadRequest());
        addDuck("Red", "Large", "5", 0).andExpect(status().isBadRequest());
        postJson("/api/ducks", "{}").andExpect(status().isBadRequest());
        postJson("/api/ducks", "not json").andExpect(status().isBadRequest());
        mvc.perform(get("/api/ducks")).andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void edit_changes_price_and_quantity_and_validates() throws Exception {
        int id = repository.saveAndFlush(new com.digitalharbor.duckstore.entity.Duck(
                com.digitalharbor.duckstore.entity.enums.Color.RED, com.digitalharbor.duckstore.entity.enums.Size.SMALL,
                new java.math.BigDecimal("3.00"), 4)).getId();

        mvc.perform(put("/api/ducks/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"price\":4.5,\"quantity\":9}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.price").value(4.5))
                .andExpect(jsonPath("$.quantity").value(9));
        mvc.perform(put("/api/ducks/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"price\":4.5,\"quantity\":-1}")).andExpect(status().isBadRequest());
        mvc.perform(put("/api/ducks/9999").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"price\":4.5,\"quantity\":1}")).andExpect(status().isNotFound());
    }

    @Test
    void delete_hides_the_duck_and_keeps_the_record() throws Exception {
        addDuck("Green", "Small", "2", 3).andExpect(status().isCreated());
        int id = repository.findAll().get(0).getId();

        mvc.perform(delete("/api/ducks/" + id)).andExpect(status().isNoContent());
        mvc.perform(get("/api/ducks")).andExpect(jsonPath("$.length()").value(0));
        mvc.perform(delete("/api/ducks/" + id)).andExpect(status().isNotFound());
        org.assertj.core.api.Assertions.assertThat(repository.count()).isEqualTo(1);
    }

    @Test
    void unknown_url_is_404_not_500() throws Exception {
        mvc.perform(get("/api/nothing")).andExpect(status().isNotFound());
    }

    // ---------- store ----------

    @Test
    void order_returns_package_protection_total_and_breakdown() throws Exception {
        addDuck("Red", "XLarge", "200", 10000);
        order("Red", "XLarge", 10, "USA", "Sea").andExpect(status().isOk())
                .andExpect(jsonPath("$.packageType").value("Wood"))
                .andExpect(jsonPath("$.protections[0]").value("Moisture-absorbing beads"))
                .andExpect(jsonPath("$.protections[1]").value("Bubble-wrap bags"))
                .andExpect(jsonPath("$.subtotal").value(2000.0))
                .andExpect(jsonPath("$.breakdown.length()").value(3))
                .andExpect(jsonPath("$.totalToPay").value(2860.0));
    }

    @Test
    void order_uses_cheapest_live_price() throws Exception {
        addDuck("Yellow", "Small", "7", 10);
        addDuck("Yellow", "Small", "4", 10);
        order("Yellow", "Small", 1, "India", "Land").andExpect(jsonPath("$.unitPrice").value(4.0));
    }

    @Test
    void order_for_a_missing_duck_is_404() throws Exception {
        order("Black", "XSmall", 1, "USA", "Air").andExpect(status().isNotFound());
    }

    @Test
    void order_validates_input() throws Exception {
        addDuck("Red", "Large", "10", 10);
        order("Red", "Large", 0, "USA", "Air").andExpect(status().isBadRequest());
        order("Red", "Large", 1, "", "Air").andExpect(status().isBadRequest());
        order("Red", "Large", 1, "USA", "Rocket").andExpect(status().isBadRequest());
        order("Purple", "Large", 1, "USA", "Air").andExpect(status().isBadRequest());
    }
}
