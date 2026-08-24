package com.webelement.taskapp.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.webelement.taskapp.entity.ClientEntity;

public interface ClientRepository extends JpaRepository<ClientEntity, Integer>{

}
