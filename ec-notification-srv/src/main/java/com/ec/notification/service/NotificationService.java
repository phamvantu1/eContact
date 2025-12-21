package com.ec.notification.service;

import com.ec.notification.mapper.EmailMapper;
import com.ec.notification.mapper.MessageMapper;
import com.ec.notification.mapper.NoticeMapper;
import com.ec.notification.model.dto.SendEmailDTO;
import com.ec.notification.model.entity.Email;
import com.ec.notification.model.entity.Message;
import com.ec.notification.model.entity.Notice;
import com.ec.notification.repository.EmailRepository;
import com.ec.notification.repository.MessageRepository;
import com.ec.notification.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

    private String setContendNotice(SendEmailDTO request){
        return request.getTitleEmail() + ": " + request.getContractName();
    }


    @Transactional
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

            Notice notice = Notice.builder()
                    .contractNo(request.getContractNo())
                    .noticeContent(this.setContendNotice(request))
                    .noticeUrl(request.getUrl())
                    .email(request.getRecipientEmail())
                    .isRead(false)
                    .build();

            emailService.sendEmail(emailMapper.toDTO(email));

            emailRepository.save(email);

            noticeRepository.save(notice);

        }catch (Exception e){
            log.error("Failed to send email notification contractNo :  {}",  request.getContractNo());
        }
    }

    public List<Notice> getAllNotice(Authentication authentication,
                                     int page,
                                     int size) {
       try{
           String email = authentication.getName();

           Pageable pageable = PageRequest.of(page, size);

           Page<Notice> noticePage = noticeRepository.findAllByEmail(email,pageable);

           return noticePage.getContent();
       }catch (Exception e){
           log.error("Error retrieving notices: {}", e.getMessage());
           throw new RuntimeException("Failed to retrieve notices", e);
       }
    }

    @Transactional
    public Notice readNotice(Integer id) {
        try{
            Notice notice = noticeRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Notice not found with id: " + id));

            notice.setRead(true);

            return noticeRepository.save(notice);
        }catch (Exception e){
            log.error("Error marking notice as read: {}", e.getMessage());
            throw new RuntimeException("Failed to mark notice as read", e);
        }
    }
}
