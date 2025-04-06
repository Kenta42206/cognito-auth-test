package com.example.cognitotest.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class AppUser {
	
    public AppUser() {
    }

    public AppUser(String username, String password) {
		this.username = username;
		this.password = password;
	}

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "username", nullable = false, length = 50)
    private String username;
    
    @Column(nullable=false)
    private String password;
}
