package com.collaberadigital.cove.repository.repositoryIml;

import org.springframework.stereotype.Repository;

import com.collaberadigital.cove.model.entity.UserEntity;
import com.collaberadigital.cove.repository.CustomerDao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import reactor.core.publisher.Mono;

@Repository
public class CustomerDaoImpl implements CustomerDao {

	 @PersistenceContext
	    private EntityManager entityManager;

	    @Override
	    public Mono<UserEntity> findByEmailReactive(String email) {
	        return Mono.fromCallable(() -> {
	        	UserEntity user = entityManager.createQuery(
	                    "SELECT u FROM UserEntity u WHERE u.email = :email", UserEntity.class)
	                    .setParameter("email", email)
	                    .getSingleResult();

	            return user;
	        });
	    }

}
