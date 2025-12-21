package com.ec.notification.repository;

import com.ec.notification.model.entity.Notice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoticeRepository extends JpaRepository<Notice,Integer> {

    Page<Notice> findAllByEmail(String email, Pageable pageable);
}
