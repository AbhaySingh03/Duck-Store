package com.digitalharbor.duckstore.service;

import com.digitalharbor.duckstore.common.ConflictException;
import com.digitalharbor.duckstore.common.NotFoundException;
import com.digitalharbor.duckstore.entity.enums.Color;
import com.digitalharbor.duckstore.entity.enums.Size;
import com.digitalharbor.duckstore.repository.DuckRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class DuckServiceTest {

    @Autowired DuckService service;
    @Autowired DuckRepository repository;

    @BeforeEach
    void clean() {
        repository.deleteAll();
    }

    private AddResult add(Color c, Size s, String price, int qty) {
        return service.add(c, s, new BigDecimal(price), qty);
    }

    @Test
    void adding_a_new_duck_creates_a_record() {
        AddResult r = add(Color.RED, Size.XLARGE, "200", 10);
        assertThat(r.created()).isTrue();
        assertThat(service.list(Sort.Direction.DESC)).hasSize(1);
    }

    @Test
    void same_color_size_price_merges_quantities() {
        add(Color.RED, Size.XLARGE, "200", 10);
        AddResult r = add(Color.RED, Size.XLARGE, "200.00", 5);   // 200 and 200.00 are the same price
        assertThat(r.created()).isFalse();
        assertThat(repository.count()).isEqualTo(1);
        assertThat(r.duck().getQuantity()).isEqualTo(15);
    }

    @Test
    void different_price_color_or_size_creates_separate_records() {
        add(Color.RED, Size.XLARGE, "200", 1);
        add(Color.RED, Size.XLARGE, "200.01", 1);
        add(Color.GREEN, Size.XLARGE, "200", 1);
        add(Color.RED, Size.LARGE, "200", 1);
        assertThat(repository.count()).isEqualTo(4);
    }

    @Test
    void concurrent_adds_of_the_same_duck_are_all_counted_and_never_duplicated() throws Exception {
        int threads = 10;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<AddResult>> futures = new ArrayList<>();
        for (int i = 0; i < threads; i++) {
            futures.add(pool.submit(() -> {
                start.await();
                return add(Color.YELLOW, Size.MEDIUM, "9.99", 1);
            }));
        }
        start.countDown();
        for (Future<AddResult> f : futures) {
            f.get(30, TimeUnit.SECONDS);
        }
        pool.shutdown();

        List<Duck> all = repository.findAll();
        assertThat(all).hasSize(1);
        assertThat(all.get(0).getQuantity()).isEqualTo(threads);
    }

    @Test
    void delete_is_logical_and_hidden_from_the_list() {
        Duck duck = add(Color.BLACK, Size.SMALL, "5", 3).duck();
        service.delete(duck.getId());

        assertThat(service.list(Sort.Direction.DESC)).isEmpty();
        assertThat(repository.findById(duck.getId())).get().extracting(Duck::isDeleted).isEqualTo(true);
        assertThatThrownBy(() -> service.delete(duck.getId())).isInstanceOf(NotFoundException.class);
    }

    @Test
    void adding_a_deleted_duck_again_brings_it_back_with_only_the_new_quantity() {
        Duck duck = add(Color.BLACK, Size.SMALL, "5", 3).duck();
        service.delete(duck.getId());

        AddResult r = add(Color.BLACK, Size.SMALL, "5", 7);
        assertThat(r.duck().getId()).isEqualTo(duck.getId());
        assertThat(r.duck().getQuantity()).isEqualTo(7);
        assertThat(service.list(Sort.Direction.DESC)).hasSize(1);
    }

    @Test
    void update_changes_price_and_quantity_only() {
        Duck duck = add(Color.GREEN, Size.LARGE, "10", 1).duck();
        Duck updated = service.update(duck.getId(), new BigDecimal("12.5"), 40);
        assertThat(updated.getPrice()).isEqualByComparingTo("12.50");
        assertThat(updated.getQuantity()).isEqualTo(40);
        assertThat(updated.getColor()).isEqualTo(Color.GREEN);
        assertThat(updated.getSize()).isEqualTo(Size.LARGE);
    }

    @Test
    void update_to_a_price_that_already_exists_is_rejected() {
        add(Color.GREEN, Size.LARGE, "10", 1);
        Duck other = add(Color.GREEN, Size.LARGE, "11", 1).duck();
        assertThatThrownBy(() -> service.update(other.getId(), new BigDecimal("10"), 5))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void update_of_a_deleted_or_unknown_duck_is_not_found() {
        Duck duck = add(Color.RED, Size.SMALL, "1", 1).duck();
        service.delete(duck.getId());
        assertThatThrownBy(() -> service.update(duck.getId(), BigDecimal.ONE, 1)).isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> service.update(9999, BigDecimal.ONE, 1)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void list_is_sorted_by_quantity() {
        add(Color.RED, Size.SMALL, "1", 5);
        add(Color.RED, Size.MEDIUM, "1", 50);
        add(Color.RED, Size.LARGE, "1", 20);
        assertThat(service.list(Sort.Direction.DESC)).extracting(Duck::getQuantity).containsExactly(50, 20, 5);
        assertThat(service.list(Sort.Direction.ASC)).extracting(Duck::getQuantity).containsExactly(5, 20, 50);
    }

    @Test
    void store_price_is_the_cheapest_live_duck_of_that_color_and_size() {
        add(Color.RED, Size.MEDIUM, "8", 1);
        Duck cheapest = add(Color.RED, Size.MEDIUM, "5", 1).duck();
        add(Color.RED, Size.MEDIUM, "6", 1);
        assertThat(service.currentPrice(Color.RED, Size.MEDIUM)).isEqualByComparingTo("5");

        service.delete(cheapest.getId());   // deleted ducks are ignored
        assertThat(service.currentPrice(Color.RED, Size.MEDIUM)).isEqualByComparingTo("6");
        assertThatThrownBy(() -> service.currentPrice(Color.BLACK, Size.XSMALL)).isInstanceOf(NotFoundException.class);
    }
}
