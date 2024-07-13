package com.sapient.auth_service.repository;


import com.sapient.auth_service.modal.User;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface  UserRepository extends MongoRepository<User, String> {
   User findByEmail(String email);
}
