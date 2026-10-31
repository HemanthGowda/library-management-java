package com.library.management.user.repository;

import com.library.management.user.model.UserDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserRepository extends MongoRepository<UserDocument, String> {

    Optional<UserDocument> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);
}
