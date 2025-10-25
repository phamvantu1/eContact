package com.ec.contract.controller;

import com.ec.contract.model.dto.ParticipantDTO;
import com.ec.contract.service.ParticipantService;
import com.ec.library.response.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/participants")
@RequiredArgsConstructor
public class ParticipantController {

    private final ParticipantService participantService;

    @PostMapping("/create-participant/{contractId}")
    public Response<?> createParticipant(@RequestBody List<ParticipantDTO> participantDTOList,
                                         @PathVariable("contractId") Integer contractId) {

        return Response.success(participantService.createParticipant(participantDTOList, contractId));

    }
}
