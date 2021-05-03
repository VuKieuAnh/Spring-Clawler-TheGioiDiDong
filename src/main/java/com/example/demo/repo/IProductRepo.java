package com.example.demo.repo;

import com.example.demo.model.Product;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IProductRepo extends CrudRepository<Product, Long> {
}