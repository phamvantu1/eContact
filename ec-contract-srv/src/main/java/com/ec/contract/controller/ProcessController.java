package com.ec.contract.controller;

import com.ec.contract.model.dto.ParticipantDTO;
import com.ec.contract.model.dto.RecipientDTO;
import com.ec.contract.service.*;
import com.ec.library.response.Response;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@RestController
@RequestMapping("/processes")
@AllArgsConstructor
@Slf4j
public class ProcessController {
    private final ProcessService processService;
    private final ParticipantService participantService;
    private final RecipientService recipientService;
    private final FieldService fieldService;
    private final DocumentService documentService;
    private final ModelMapper modelMapper;
    private final CustomerService customerService;

    @PutMapping("/coordinator/{participantId}/{recipientId}")
    public Response<?> coordinator(
            Authentication authentication,
            @PathVariable("participantId") int participantId,
            @PathVariable("recipientId") int recipientId,
            @RequestBody @Valid Collection<RecipientDTO> recipientDtoCollection) {
        final var participantDtoOptional = participantService.updateRecipientForCoordinator(
                authentication,
                participantId,
                recipientId,
                recipientDtoCollection
        );

        return participantDtoOptional.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

}
