package com.jk.limited_stock_drop.specification;

import com.jk.limited_stock_drop.entity.Product;
import com.jk.limited_stock_drop.enums.Category;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

public class ProductSpecification {

    public static Specification<Product> hasSearch(String search) {
        return (root, query, cb) -> {
            if (search == null || search.isBlank()) return null;
            String pattern = "%" + search.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("name")), pattern),
                    cb.like(cb.lower(root.get("description")), pattern)
            );
        };
    }

    public static Specification<Product> hasCategory(Category category) {
        return (root, query, cb) ->
                category == null ? null : cb.equal(root.get("category"), category);
    }

    public static Specification<Product> isInStock(Boolean inStock) {
        return (root, query, cb) -> {
            if (inStock == null || Boolean.FALSE.equals(inStock)) return null;
            // Use database fields: totalStock > reservedStock
            return cb.greaterThan(
                    cb.diff(root.get("totalStock"), root.get("reservedStock")),
                    0
            );
        };
    }

    public static Specification<Product> hasPriceBetween(BigDecimal min, BigDecimal max) {
        return (root, query, cb) -> {
            if (min == null && max == null) return null;
            if (min == null) return cb.lessThanOrEqualTo(root.get("price"), max);
            if (max == null) return cb.greaterThanOrEqualTo(root.get("price"), min);
            return cb.between(root.get("price"), min, max);
        };
    }
}
