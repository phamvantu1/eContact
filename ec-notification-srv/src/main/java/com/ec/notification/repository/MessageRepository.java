package com.ec.notification.repository;

import com.ec.notification.model.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, Integer> {

    Message findByCode(String code);
}
