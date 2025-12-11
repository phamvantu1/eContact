package com.ec.notification.service;

import com.ec.notification.mapper.EmailMapper;
import com.ec.notification.mapper.MessageMapper;
import com.ec.notification.mapper.NoticeMapper;
import com.ec.notification.model.dto.SendEmailDTO;
import com.ec.notification.model.entity.Email;
import com.ec.notification.model.entity.Message;
import com.ec.notification.repository.EmailRepository;
import com.ec.notification.repository.MessageRepository;
import com.ec.notification.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {

    private final EmailRepository emailRepository;
    private final MessageRepository messageRepository;
    private final NoticeRepository noticeRepository;
    private final EmailService emailService;
    private final EmailMapper emailMapper;
    private final NoticeMapper noticeMapper;
    private final MessageMapper messageMapper;

    public void sendEmailNotification(SendEmailDTO request) {
        try{

            log.info("Sending email notification to {}", request.getRecipient());

            Message message = messageRepository.findByCode(request.getCode());

            Email email = Email.builder()
                    .subject(request.getSubject())
                    .recipient(request.getRecipient())
                    .cc(request.getCc())
                    .content(message != null ? message.getMailTemplate() : "")
                    .status(request.getStatus())
                    .build();

            emailService.sendEmail(emailMapper.toDTO(email));

            emailRepository.save(email);

        }catch (Exception e){
            log.error("Failed to send email notification", e);
        }
    }
}
