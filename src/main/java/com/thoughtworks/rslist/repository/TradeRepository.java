package com.thoughtworks.rslist.repository;

import com.thoughtworks.rslist.dto.TradeDTO;
import org.springframework.data.repository.CrudRepository;

public interface TradeRepository extends CrudRepository<TradeDTO, Integer> {
}
