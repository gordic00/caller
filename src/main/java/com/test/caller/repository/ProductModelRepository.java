package com.test.caller.repository;

import com.test.caller.model.ProductModel;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProductModelRepository extends MongoRepository<ProductModel, String> {
}
