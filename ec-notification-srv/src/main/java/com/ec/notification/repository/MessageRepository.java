package com.ec.notification.repository;

import com.ec.notification.model.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface MessageRepository extends JpaRepository<Message, Integer> {

    @Query(value = "select m.* from messages m where m.code = :code " +
            "limit 1 "
            , nativeQuery = true)
    Message findByCode(String code);
}
