//package com.ec.contract.service;
//
//import com.ec.contract.constant.ContractStatus;
//import com.ec.contract.model.dto.ShareListDto;
//import com.ec.contract.repository.ContractRepository;
//import com.ec.contract.repository.ShareRepository;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.Objects;
//import java.util.Optional;
//
//@Service
//@Slf4j
//@RequiredArgsConstructor
//public class ShareService {
//
//    private final ShareRepository shareRepository;
//    private final ContractRepository contractRepository;
//
//
//    @Transactional
//    public Optional<ShareListDto> create(ShareListDto shareListDto) {
//        final var contractOptional = contractRepository.findById(shareListDto.getContractId());
//        if (contractOptional.isEmpty() || !Objects.equals(contractOptional.get().getStatus(), ContractStatus.SIGNED.getDbVal())) {
//            return Optional.empty();
//        }
//
//        var contract = contractOptional.get();
//
//        ShareDto shareDto;
//        if (shareListDto.getEmail() != null) {
//            for (String email : shareListDto.getEmail()) {
//                if (email == null) continue;
//                email = email.trim();
//                shareDto = ShareDto.builder()
//                        .email(email)
//                        .contractId(contract.getId())
//                        .build();
//
//                Share share;
//
//                if (shareDto.getEmail() != null) {
//                    shareDto.setEmail(shareDto.getEmail().trim());
//                }
//
//                // check exists shared
//                var shareOptional = shareRepository.findFirstByContractIdAndEmail(shareDto.getContractId(), shareDto.getEmail());
//                if (shareOptional.isPresent()) {
//                    share = shareOptional.get();
//                } else {
//
//                    share = modelMapper.map(shareDto, Share.class);
//                }
//
//                // check customer
//                final var customerFound = customerService.getCustomerByEmail(shareDto.getEmail());
////                var exists = customer != null && customer.getId() > 0;
//
//                boolean exists = customerService.findCustomerByEmailLoginType(shareDto.getEmail());
//
//                String token = null;
//                var shareType = ShareType.CUSTOMER;
//                CustomerDto customer = customerFound;
//                // customer khong ton tai
//                if (!exists) {
//                    // generate token
//                    token = PasswordUtil.generateToken(8);
//                    shareType = ShareType.GUEST;
//                    customer = null;
//                }
//
//                share.setShareType(shareType);
//                share.setToken(token);
//
//                final var created = shareRepository.save(share);
//
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
//            }
//        }
//
//
//        // thực hiện chia sẻ cho số điện thoại
//        if (shareListDto.getPhone() != null) {
//            for (String phone : shareListDto.getPhone()) {
//                if (phone == null) continue;
//
//                phone = phone.trim();
//                shareDto = ShareDto.builder()
//                        .phone(phone)
//                        .email(phone)
//                        .contractId(contract.getId())
//                        .build();
//
//                Share share;
//                if (shareDto.getPhone() != null) {
//                    shareDto.setPhone(shareDto.getPhone().trim());
//                }
//
//                // check exists shared
//                var shareOptional = shareRepository.findFirstByContractIdAndPhone(shareDto.getContractId(), shareDto.getPhone());
//                if (shareOptional.isPresent()) {
//                    share = shareOptional.get();
//                } else {
//                    share = modelMapper.map(shareDto, Share.class);
//                }
//
//                // check customer
////                final var customer = customerService.getCustomerByPhone(shareDto.getPhone());
////                var exists = customer != null && customer.getId() > 0;
//                boolean exists = customerService.findCustomerByPhone(shareDto.getPhone());
//
//                String token = null;
//                var shareType = ShareType.CUSTOMER;
//
//                // customer khong ton tai
//                if (!exists) {
//                    // generate token
//                    token = PasswordUtil.generateToken(8);
//                    shareType = ShareType.GUEST;
//                }
//
//                share.setShareType(shareType);
//                share.setToken(token);
//
//                final var created = shareRepository.save(share);
//
//                if (created != null) {
//
//                    OrganizationDto organizationDto = customerService.getOrganizationByCustomer(customerUser.getId()).get();
//                    log.info("organizationDto id : {}", organizationDto.getId());
//                    notificationService.notificationShareSMS(organizationDto.getId(), contract.getId(), contract.getName(), contract.getContractUid(), token, phone, "share_contract");
//                }
//            }
//        }
//
//        return Optional.of(shareListDto);
//    }
//
//}
