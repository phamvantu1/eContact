package com.ec.notification.controller;

import com.ec.notification.model.DTO.SendEmailDTO;
import com.ec.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("")
@RequiredArgsConstructor
@Tag(name = "Notification Controller", description = "APIs for managing notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/send-email")
    @Operation(summary = "Send Email Notification", description = "Sends an email notification to the specified recipient.")
    public void sendEmailNotification(@RequestBody SendEmailDTO request) {
        notificationService.sendEmailNotification(request);
    }

}
