package com.yash.duckstore.repository;

import com.yash.duckstore.domain.Duck;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DuckRepository extends JpaRepository<Duck, Long> {
}