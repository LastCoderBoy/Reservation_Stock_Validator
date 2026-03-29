package com.jk.limited_stock_drop.repository;

import com.jk.limited_stock_drop.entity.InventoryLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InventoryLogRepository extends JpaRepository<InventoryLog, Long> {
}
