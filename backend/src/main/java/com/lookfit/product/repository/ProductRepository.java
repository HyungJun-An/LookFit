package com.lookfit.product.repository;

import com.lookfit.product.domain.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, String> {
    Page<Product> findByProductCategory(String productCategory, Pageable pageable);
    List<Product> findByProductNameContaining(String productName);
}
