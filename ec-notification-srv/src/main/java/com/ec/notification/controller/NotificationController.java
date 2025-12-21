package com.ec.notification.controller;

import com.ec.library.response.Response;
import com.ec.notification.model.dto.SendEmailDTO;
import com.ec.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("")
@RequiredArgsConstructor
@Tag(name = "Notification Controller", description = "APIs for managing notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/internal/send-email")
    @Operation(summary = "Send Email Notification", description = "Sends an email notification to the specified recipient.")
    public void sendEmailNotification(@RequestBody SendEmailDTO request) {
        notificationService.sendEmailNotification(request);
    }

    @GetMapping("/get-all-notice")
    @Operation(summary = "Get All Notices", description = "Retrieves all notices from the system.")
    public Response<?> getAllNotice(Authentication authentication,
                                    @RequestParam(value = "page", defaultValue = "0") int page,
                                    @RequestParam(value = "size", defaultValue = "10") int size) {
        return Response.success(notificationService.getAllNotice(authentication, page, size));
    }

    @PutMapping("/read-notice/{id}")
    @Operation(summary = "Mark Notice as Read", description = "Marks a specific notice as read.")
    public Response<?> readNotice(@PathVariable("id") Integer id) {
        return Response.success(notificationService.readNotice(id));
    }


}
