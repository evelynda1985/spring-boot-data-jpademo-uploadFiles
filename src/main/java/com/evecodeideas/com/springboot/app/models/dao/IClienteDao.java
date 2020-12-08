package com.evecodeideas.com.springboot.app.models.dao;

import com.evecodeideas.com.springboot.app.models.entity.Cliente;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface IClienteDao extends PagingAndSortingRepository<Cliente, Long> {
}
