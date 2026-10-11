package com.yash.duckstore.repository;

import com.yash.duckstore.domain.Duck;
import com.yash.duckstore.domain.DuckColor;
import com.yash.duckstore.domain.DuckSize;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface DuckRepository extends JpaRepository<Duck, Long> {

    List<Duck> findAllByDeletedFalseOrderByQuantityAscIdAsc();

    Optional<Duck> findByIdAndDeletedFalse(Long id);

    Optional<Duck> findFirstByColorAndSizeAndDeletedFalseOrderByPriceAscIdAsc(
            DuckColor color, DuckSize size);

    Optional<Duck> findByColorAndSizeAndPriceAndDeletedFalse(
            DuckColor color, DuckSize size, BigDecimal price);

    boolean existsByColorAndSizeAndPriceAndDeletedFalseAndIdNot(
            DuckColor color, DuckSize size, BigDecimal price, Long id);

    @Modifying
    @Query(value = """
            INSERT INTO ducks (color, size, price, quantity, deleted, version, created_at, updated_at)
            VALUES (:color, :size, :price, :quantity, false, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
            ON CONFLICT (color, size, price) WHERE deleted = false
            DO UPDATE SET quantity = ducks.quantity + EXCLUDED.quantity,
                          version = ducks.version + 1,
                          updated_at = CURRENT_TIMESTAMP
            """, nativeQuery = true)
    int upsertAndMergeQuantity(
            @Param("color") String color,
            @Param("size") String size,
            @Param("price") BigDecimal price,
            @Param("quantity") int quantity);
}
