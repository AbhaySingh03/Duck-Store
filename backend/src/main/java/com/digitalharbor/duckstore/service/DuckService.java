package com.digitalharbor.duckstore.service;

import com.digitalharbor.duckstore.common.ConflictException;
import com.digitalharbor.duckstore.common.NotFoundException;
import com.digitalharbor.duckstore.entity.Duck;
import com.digitalharbor.duckstore.entity.enums.Color;
import com.digitalharbor.duckstore.entity.enums.Size;
import com.digitalharbor.duckstore.repository.DuckRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Warehouse business rules.
 *
 * Merge invariant: at most one row per (color, size, price). It is guaranteed by
 *  1. a UNIQUE constraint in the database (the final safety net), and
 *  2. a row lock + retry here, so concurrent adds of the same duck end up summed, never duplicated or lost.
 */
@Service
public class DuckService {

    private static final int MAX_ATTEMPTS = 5;

    private final DuckRepository ducks;
    private final TransactionTemplate tx;

    public DuckService(DuckRepository ducks, PlatformTransactionManager transactionManager) {
        this.ducks = ducks;
        this.tx = new TransactionTemplate(transactionManager);
    }

    public List<Duck> list(Sort.Direction quantityOrder) {
        return ducks.findByDeletedFalse(Sort.by(quantityOrder, "quantity").and(Sort.by(Sort.Direction.ASC, "id")));
    }

    public AddResult add(Color color, Size size, BigDecimal price, int quantity) {
        BigDecimal unitPrice = money(price);
        for (int attempt = 1; ; attempt++) {
            try {
                return tx.execute(status -> addOnce(color, size, unitPrice, quantity));
            } catch (DataIntegrityViolationException | ConcurrencyFailureException e) {
                // Another request inserted the same duck (or deadlocked with us) first: retry, we will now merge into it.
                if (attempt == MAX_ATTEMPTS) {
                    throw e;
                }
            }
        }
    }

    private AddResult addOnce(Color color, Size size, BigDecimal price, int quantity) {
        Duck existing = ducks.findByColorAndSizeAndPrice(color, size, price).orElse(null);
        if (existing == null) {
            return new AddResult(ducks.saveAndFlush(new Duck(color, size, price, quantity)), true);
        }
        if (existing.isDeleted()) { // bring the deleted record back; its old stock is not resurrected
            existing.setDeleted(false);
            existing.setQuantity(quantity);
            return new AddResult(existing, true);
        }
        existing.setQuantity(Math.addExact(existing.getQuantity(), quantity));
        return new AddResult(existing, false);
    }

    public Duck update(int id, BigDecimal newPrice, int quantity) {
        BigDecimal price = money(newPrice);
        return tx.execute(status -> {
            Duck duck = findLiveForUpdate(id);
            if (duck.getPrice().compareTo(price) != 0) {
                ducks.findByColorAndSizeAndPrice(duck.getColor(), duck.getSize(), price).ifPresent(other -> {
                    throw new ConflictException("A " + duck.getColor().label() + " " + duck.getSize().label()
                            + " duck priced " + price + " already exists (id " + other.getId()
                            + "). Edit that one instead or pick another price.");
                });
            }
            duck.setPrice(price);
            duck.setQuantity(quantity);
            return duck;
        });
    }

    public void delete(int id) {
        tx.executeWithoutResult(status -> findLiveForUpdate(id).setDeleted(true));
    }

    /** Unit price used by the store: the cheapest live duck of that color and size. */
    public BigDecimal currentPrice(Color color, Size size) {
        return ducks.findFirstByColorAndSizeAndDeletedFalseOrderByPriceAsc(color, size)
                .map(Duck::getPrice)
                .orElseThrow(() -> new NotFoundException(
                        "No " + color.label() + " " + size.label() + " ducks available in the warehouse"));
    }

    private Duck findLiveForUpdate(int id) {
        return ducks.findWithLockById(id).filter(d -> !d.isDeleted())
                .orElseThrow(() -> new NotFoundException("Duck " + id + " not found"));
    }

    private static BigDecimal money(BigDecimal value) {
        return value.setScale(2, RoundingMode.UNNECESSARY);
    }
}
