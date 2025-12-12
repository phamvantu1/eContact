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

            log.info("Sending email notification to {}", request.getRecipientEmail());

            Message message = messageRepository.findByCode(request.getCode());

            String template = message.getMailTemplate();

            template = template.replace("{recipientName}", request.getRecipientName());
            template = template.replace("{contractNo}", request.getContractNo());
            template = template.replace("{contractName}", request.getContractName());
            template = template.replace("{note}", request.getNote());
            template = template.replace("{sendFrom}", request.getSenderName());
            template = template.replace("{titleEmail}", request.getTitleEmail());
            template = template.replace("{actionButton}", request.getActionButton());

            log.info("Email template after replacement: {}", template);

            Email email = Email.builder()
                    .subject(request.getSubject())
                    .recipient(request.getRecipientEmail())
                    .cc(request.getCc())
                    .content(template)
                    .status(request.getStatus())
                    .build();

            emailService.sendEmail(emailMapper.toDTO(email));

            emailRepository.save(email);

        }catch (Exception e){
            log.error("Failed to send email notification contractNo :  {}",  request.getContractNo());
        }
    }
}
