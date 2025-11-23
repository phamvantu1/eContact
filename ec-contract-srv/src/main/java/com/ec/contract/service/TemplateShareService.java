package com.ec.contract.service;

import com.ec.contract.constant.BaseStatus;
import com.ec.contract.mapper.TemplateContractMapper;
import com.ec.contract.mapper.TemplateShareMapper;
import com.ec.contract.model.dto.ShareDto;
import com.ec.contract.model.dto.ShareListDto;
import com.ec.contract.model.dto.response.ContractResponseDTO;
import com.ec.contract.model.entity.TemplateContract;
import com.ec.contract.model.entity.TemplateShare;
import com.ec.contract.repository.TemplateContractRepository;
import com.ec.contract.repository.TemplateShareRepository;
import com.ec.contract.util.PasswordUtil;
import com.ec.library.exception.CustomException;
import com.ec.library.exception.ResponseCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TemplateShareService {

    private final TemplateContractRepository templateContractRepository;
    private final TemplateShareRepository templateShareRepository;
    private final TemplateShareMapper templateShareMapper;
    private final CustomerService customerService;
    private final TemplateContractMapper templateContractMapper;

    @Transactional
    public Object createShare(ShareListDto shareListDto) {
        try {

            final var contractOptional = templateContractRepository.findById(shareListDto.getContractId());

            if (contractOptional.isEmpty()) {
                throw new CustomException(ResponseCode.CONTRACT_NOT_FOUND);
            }

            var contract = contractOptional.get();

            ShareDto shareDto;
            if (shareListDto.getEmail() != null) {

                log.info("Creating shares for contract ID {}: {}", contract.getId(), shareListDto.getEmail());

                for (String email : shareListDto.getEmail()) {
                    if (email == null) continue;
                    email = email.trim();
                    shareDto = ShareDto.builder()
                            .email(email)
                            .contractId(contract.getId())
                            .build();

                    TemplateShare share;

                    if (shareDto.getEmail() != null) {
                        shareDto.setEmail(shareDto.getEmail().trim());
                    }

                    // check exists shared
                    var shareOptional = templateShareRepository.findFirstByContractIdAndEmail(shareDto.getContractId(), shareDto.getEmail());
                    if (shareOptional.isPresent()) {
                        share = shareOptional.get();
                    } else {

                        share = templateShareMapper.toEntity(shareDto);
                    }

                    // check customer
                    final var customerFound = customerService.getCustomerByEmail(shareDto.getEmail());

                    String password = null;

                    // customer khong ton tai
                    if (customerFound == null) {
                        // generate token
                        password = PasswordUtil.generateToken(8);

                    }

                    share.setPassword(password);
                    share.setStatus(BaseStatus.ACTIVE.ordinal());

                    final var created = templateShareRepository.save(share);

//                if (created != null) {
//                    OrganizationDto organizationDto = customerService.getOrganizationByCustomer(customerUser.getId()).get();
//
//                    ContractShareNoticeRequest request = ContractShareNoticeRequest.builder()
//                            .customerName(exists ? customer.getName() : shareDto.getEmail())
//                            .senderName(customerUser.getName())
//                            .senderParticipant(organizationDto.getName())
//                            .contractName(contract.getName())
//                            .email(shareDto.getEmail())
//                            .phone(customer != null ? customer.getPhone() : null)
//                            .accessCode(token)
//                            .loginType(exists ? "0" : "1")
//                            .contractUid(contract.getContractUid())
//                            .contractUrl("" + contract.getId())
//                            .contractId(contract.getId())
//                            .orgId(organizationDto.getId())
//                            .build();
//
//                    notificationService.notification(request);
//                }
                }
            }

            return Optional.of(shareListDto);
        } catch (CustomException ce) {
            throw ce;
        } catch (Exception e) {
            log.error("Error in createShare: ", e);
            return Optional.empty();
        }
    }

    @Transactional(readOnly = true)
    public Page<ContractResponseDTO> getAllSharesContract(String textSearch,
                                                          String fromDate,
                                                          String toDate,
                                                          Integer page,
                                                          Integer size,
                                                          Authentication authentication) {
        try {
            String email = authentication.getName();

            Pageable pageable = PageRequest.of(page, size);

            Page<TemplateContract> contractPage = templateShareRepository.getAllSharesContract(
                    email,
                    textSearch,
                    fromDate,
                    toDate,
                    pageable
            );

            List<ContractResponseDTO> contractResponseDTOS = contractPage.getContent().stream()
                    .map(templateContractMapper::toDto)
                    .toList();

            return new PageImpl<>(contractResponseDTOS, pageable, contractPage.getTotalElements());

        } catch (CustomException ce) {
            throw ce;
        } catch (Exception e) {
            log.error("Error in getAllShares: ", e);
            throw new RuntimeException("ERROR_WHILE_FETCHING_SHARES");
        }
    }

}
