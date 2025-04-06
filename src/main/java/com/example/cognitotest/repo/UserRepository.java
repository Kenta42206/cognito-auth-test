package com.example.cognitotest.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.cognitotest.entity.AppUser;

public interface UserRepository extends JpaRepository<AppUser, Integer>{

	Optional<AppUser> findByUsername(String username);

}
