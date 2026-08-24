package com.webelement.taskapp.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.webelement.taskapp.entity.TransactionEntity;


public interface TransactionRepo extends JpaRepository<TransactionEntity, Integer> {

}
