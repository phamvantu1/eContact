package com.ec.contract.service;

import com.ec.contract.constant.BaseStatus;
import com.ec.contract.constant.ContractStatus;
import com.ec.contract.mapper.ContractMapper;
import com.ec.contract.mapper.ShareMapper;
import com.ec.contract.model.dto.ShareDto;
import com.ec.contract.model.dto.ShareListDto;
import com.ec.contract.model.dto.request.FilterContractDTO;
import com.ec.contract.model.dto.response.ContractResponseDTO;
import com.ec.contract.model.entity.Contract;
import com.ec.contract.model.entity.Share;
import com.ec.contract.repository.ContractRepository;
import com.ec.contract.repository.ShareRepository;
import com.ec.contract.util.PasswordUtil;
import com.ec.library.exception.CustomException;
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
import java.util.Objects;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ShareService {

    private final ShareRepository shareRepository;
    private final ContractRepository contractRepository;
    private final CustomerService customerService;
    private final ShareMapper shareMapper;
    private final ContractMapper contractMapper;


    @Transactional
    public Optional<ShareListDto> createShare(ShareListDto shareListDto) {
        try {
            final var contractOptional = contractRepository.findById(shareListDto.getContractId());
            if (contractOptional.isEmpty() || !Objects.equals(contractOptional.get().getStatus(), ContractStatus.SIGNED.getDbVal())) {
                return Optional.empty();
            }

            var contract = contractOptional.get();

            ShareDto shareDto;
            if (shareListDto.getEmail() != null) {
                for (String email : shareListDto.getEmail()) {
                    if (email == null) continue;
                    email = email.trim();
                    shareDto = ShareDto.builder()
                            .email(email)
                            .contractId(contract.getId())
                            .build();

                    Share share;

                    if (shareDto.getEmail() != null) {
                        shareDto.setEmail(shareDto.getEmail().trim());
                    }

                    // check exists shared
                    var shareOptional = shareRepository.findFirstByContractIdAndEmail(shareDto.getContractId(), shareDto.getEmail());
                    if (shareOptional.isPresent()) {
                        share = shareOptional.get();
                    } else {

                        share = shareMapper.toEntity(shareDto);
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

                    final var created = shareRepository.save(share);

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
        } catch (Exception e) {
            log.error("Error in createShare: ", e);
            return Optional.empty();
        }
    }

    @Transactional(readOnly = true)
    public Page<ContractResponseDTO> getAllSharesContract(FilterContractDTO filterContractDTO,
                                                  Authentication authentication) {
        try {
            String email = authentication.getName();

            Pageable pageable = PageRequest.of(filterContractDTO.getPage(), filterContractDTO.getSize());

            Page<Contract> contractPage = shareRepository.getAllSharesContract(
                    email,
                    filterContractDTO.getTextSearch(),
                    filterContractDTO.getFromDate(),
                    filterContractDTO.getToDate(),
                    pageable
            );

            List<ContractResponseDTO> contractResponseDTOS = contractPage.getContent().stream()
                    .map(contractMapper::toDto)
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
