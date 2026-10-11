package com.digitalharbor.duckstore.repository;

import com.digitalharbor.duckstore.entity.Duck;
import com.digitalharbor.duckstore.entity.enums.Color;
import com.digitalharbor.duckstore.entity.enums.Size;
import jakarta.persistence.LockModeType;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

public interface DuckRepository extends JpaRepository<Duck, Integer> {

    List<Duck> findByDeletedFalse(Sort sort);

    /** Row-locked lookup (SELECT ... FOR UPDATE). Includes logically deleted ducks. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Duck> findByColorAndSizeAndPrice(Color color, Size size, BigDecimal price);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Duck> findWithLockById(Integer id);

    Optional<Duck> findFirstByColorAndSizeAndDeletedFalseOrderByPriceAsc(Color color, Size size);
}
