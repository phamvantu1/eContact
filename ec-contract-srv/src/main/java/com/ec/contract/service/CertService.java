package com.ec.contract.service;

import com.ec.contract.model.dto.CertResponse;
import com.ec.contract.model.dto.OrganizationDTO;
import com.ec.contract.model.dto.keystoreDTO.CertificateCustomersDto;
import com.ec.contract.model.dto.keystoreDTO.CertificateDto;
import com.ec.contract.model.dto.keystoreDTO.GetDataCertRequest;
import com.ec.contract.model.dto.keystoreDTO.OrganizationResponse;
import com.ec.contract.model.entity.Customer;
import com.ec.contract.model.entity.keystoreEntity.Certificate;
import com.ec.contract.model.entity.keystoreEntity.CertificateCustomer;
import com.ec.contract.repository.CertificateCustomersRepository;
import com.ec.contract.repository.CertificateMappingRepository;
import com.ec.contract.repository.CertificateRepository;
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
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.naming.ldap.LdapName;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CertService {

    private final CertificateRepository certificateRepository;
    private final CustomerService customerService;
    private final CertificateCustomersRepository certificateCustomersRepository;
    private final CertificateMappingRepository certificateMappingRepository;

    // lưu file keystore vào database
    @Transactional
    public Map<String, String> importCertToDatabase(MultipartFile multipartFile, String[] emails, String password, String status, Authentication authentication) {
        try {

            String currentEmail = authentication.getName();

            OrganizationDTO organizationDTO = customerService.getOrganizationByCustomerEmail(currentEmail);

            byte[] data = multipartFile.getBytes();

            KeyStore keyStore = KeyStore.getInstance("PKCS12");

            Optional<Certificate> certificateIsPresentFromDatabase = certificateRepository.findFirstByKeyStoreFileNameAndOrgAdminCreate(multipartFile.getOriginalFilename(), organizationDTO.getId());

            if (certificateIsPresentFromDatabase.isPresent()) {
                throw new CustomException(ResponseCode.CERT_IN_YOUR_ORGANIZATION);
            }

            try {
                keyStore.load(new ByteArrayInputStream(data), password.toCharArray());
            } catch (IOException ioException) {
                ioException.printStackTrace();
                throw new CustomException(ResponseCode.PASSWORD_INVALID);
            }

            int aliasCount = 0;
            Enumeration<String> aliases = keyStore.aliases();
            while (aliases.hasMoreElements()) {
                aliases.nextElement();
                aliasCount++;
            }
            if (aliasCount != 1) {
                throw new CustomException(ResponseCode.CERT_REQUIRE_SUB);
            }
            String alias = keyStore.aliases().nextElement();
            X509Certificate certificate = (X509Certificate) keyStore.getCertificate(alias);
            String dataSubject = certificate.getSubjectDN().getName();
            String[] dataSubjectSplit = dataSubject.split(",");
            String subjectCert = null;
            for (String a : dataSubjectSplit) {
                if (a.contains("CN=")) {
                    subjectCert = a.split("=")[1];
                }
            }
            List<CertificateCustomer> certificateCustomers = new ArrayList<>();
            if (emails != null) {
                Arrays.asList(emails).forEach(email -> {
                    Customer newCustomer = customerService.getCustomerByEmail(email);
                    if (!newCustomer.getOrganizationId().equals(organizationDTO.getId())) {
                        throw new CustomException(ResponseCode.EMAIL_NOT_IN_YOUR_ORGANIZATION);
                    }
                    Optional<CertificateCustomer> certificateCustomer = certificateCustomersRepository.findFirstByEmail(email);
                    CertificateCustomer customer = certificateCustomer.orElseGet(() -> certificateCustomersRepository.save(CertificateCustomer.builder()
                            .email(email)
                            .organizationId(organizationDTO.getId())
                            .build()));
                    certificateCustomers.add(customer);
                });
            }


            Certificate cert = Certificate.builder()
                    .keystore(data)
                    .status(status)
                    .subject(subjectCert)
                    .alias(alias)
                    .orgAdminCreate(organizationDTO.getId())
                    .certInformation(dataSubject)
                    .keystoreDateStart(certificate.getNotBefore())
                    .keystoreDateEnd(certificate.getNotAfter())
                    .keystoreSerialNumber(certificate.getSerialNumber().toString())
                    .passwordKeystore(password)
                    .keyStoreFileName(multipartFile.getOriginalFilename())
                    .issuer(certificate.getIssuerDN().getName())
                    .certificateCustomers(certificateCustomers)
                    .createAt(new Date())
                    .build();
            certificateRepository.save(cert);
            return Map.of("message", "Lưu dữ liệu file chứng thư số thành công");
        } catch (CustomException ce) {
            throw ce;
        } catch (RuntimeException e) {
            return Map.of("error", e.getMessage());
        } catch (Exception e) {
            log.error("Lưu dữ liệu file chứng thư số thất bại {}", e);
            return Map.of("error", "Lưu dữ liệu file chứng thư số thất bại");
        }
    }

    @Transactional(readOnly = true)
    public CertificateCustomersDto findCertByUser(Authentication authentication) {
        try {
            String email = authentication.getName();

            CertificateCustomersDto certificateCustomersDto = certificateCustomersRepository
                    .findByPhoneOrEmailAndLoginType(email)
                    .map(CertificateCustomersDto::EntityToDto)
                    .orElseGet(() -> CertificateCustomersDto.builder().build());

            if (certificateCustomersDto.getCertificates() == null) {
                certificateCustomersDto.setCertificates(new ArrayList<>());
            }

            List<CertificateCustomer> certificateCustomersDtoLoginByEmailAndSDT = certificateCustomersRepository
                    .findByPhoneOrEmailAndLoginTypeByEmailAndSDT(email);

            List<CertificateCustomersDto> certificateCustomersDtos = certificateCustomersDtoLoginByEmailAndSDT
                    .stream().map(CertificateCustomersDto::EntityToDto).collect(Collectors.toList());

            for (CertificateCustomersDto otherDto : certificateCustomersDtos) {
                if (otherDto.getCertificates() != null) {
                    for (CertificateDto cert : otherDto.getCertificates()) {
                        if ("1".equals(cert.getStatus()) && !certificateCustomersDto.getCertificates().contains(cert)) {
                            certificateCustomersDto.getCertificates().add(cert);
                        }
                    }
                }
            }

            if (certificateCustomersDto.getId() == null) {
                certificateCustomersDto.setId(certificateCustomersDtoLoginByEmailAndSDT.get(0).getId());
                certificateCustomersDto.setPhone(certificateCustomersDtoLoginByEmailAndSDT.get(0).getPhone());
                certificateCustomersDto.setEmail(certificateCustomersDtoLoginByEmailAndSDT.get(0).getEmail());
                certificateCustomersDto.setOrganization_id(certificateCustomersDtoLoginByEmailAndSDT.get(0).getOrganizationId());
            }

            if (!(StringUtils.hasText(certificateCustomersDto.getEmail())) && !(StringUtils.hasText(certificateCustomersDto.getPhone()))) {
                return certificateCustomersDto;
            }
            List<CertificateDto> dataRemove = certificateCustomersDto.getCertificates().stream().filter(a -> !(a.getStatus().equals("1"))).collect(Collectors.toList());
            certificateCustomersDto.getCertificates().removeAll(dataRemove);
            return certificateCustomersDto;
        } catch (Exception e) {
            log.error("Lấy dữ liệu chứng thư số theo user thất bại {}", e);
            throw new RuntimeException("Lấy dữ liệu chứng thư số theo user thất bại");
        }
    }

    @Transactional
    public Map<String, String> updateUserFromCert(Integer certificateId, String[] emails, String status, Authentication authentication) {

        try {
            String currentEmail = authentication.getName();

            OrganizationDTO organizationDTO = customerService.getOrganizationByCustomerEmail(currentEmail);

            Optional<Certificate> certificate = certificateRepository.findById(certificateId);
            if (certificate.isPresent()) {

                if (StringUtils.hasText(status)) {
                    certificate.get().setStatus(status);
                }

                boolean checkOrgFromCert = false;

                List<Integer> OrgCustomersFromCert = certificate.get().getCertificateCustomers()
                        .stream().map(CertificateCustomer::getOrganizationId)
                        .collect(Collectors.toList());

                for (Integer orgFromCert : OrgCustomersFromCert) {
                    if (organizationDTO.getId().equals(orgFromCert)) {
                        checkOrgFromCert = true;
                        break;
                    }
                }
                if (!checkOrgFromCert) {
                    if (!(certificate.get().getOrgAdminCreate().equals(organizationDTO.getId()))) {
                        throw new RuntimeException("Tổ chức bạn không được cấp quyền quản lý chứng thư số này");
                    }
                }
                List<String> dataEmailFromCert = certificate.get().getCertificateCustomers().stream()
                        .map(CertificateCustomer::getEmail)
                        .collect(Collectors.toList());


                if (emails != null) {
                    Arrays.asList(emails).forEach(e -> {
                        Customer customerEmail = customerService.getCustomerByEmail(e);
                        if (customerEmail == null) {
                            throw new RuntimeException("Email không tồn tại trong hệ thống : ".concat(e));
                        }
                        Optional<CertificateCustomer> customer = certificateCustomersRepository.findFirstByEmail(e);
                        if (!(dataEmailFromCert.contains(e))) {
                            certificate.get().getCertificateCustomers().add(customer.orElseGet(() ->
                                    certificateCustomersRepository.save(CertificateCustomer.builder()
                                            .email(e)
                                            .organizationId(customerEmail.getOrganizationId())
                                            .build())));
                        }
                        certificateRepository.save(certificate.get());
                    });
                }

            }
            return Map.of("message", "Cập nhật dữ liệu chứng thư số thành công");
        } catch (Exception e) {
            log.error("Cập nhật dữ liệu chứng thư số thất bại {}", e);
            return Map.of("error", "Cập nhật dữ liệu chứng thư số thất bại");
        }
    }

    @Transactional
    public Map<String, String> removeUserFromCert(Integer certificateId, Integer[] customerIds, Authentication authentication) {

        try {

            Optional<Certificate> certificate = certificateRepository.findById(certificateId);

            if (certificate.isPresent()) {
                Arrays.asList(customerIds).forEach(c -> {

                    CertificateCustomer customer = certificateCustomersRepository.findById(c)
                            .orElseThrow(() -> new CustomException(ResponseCode.USER_DONT_EXIST));

                    certificateMappingRepository.deleteCertificateMappingByCertificateAndCertificateCustomer(certificate.get(), customer);
                });

            } else {
                throw new RuntimeException("Dữ liêu cert không tồn tại");
            }
            return Map.of("message", "Xóa người dùng khỏi chứng thư số thành công");
        } catch (CustomException ce) {
            throw ce;
        } catch (Exception e) {
            log.error("Xóa người dùng khỏi chứng thư số thất bại {}", e);
            return Map.of("error", "Xóa người dùng khỏi chứng thư số thất bại");
        }
    }

    @Transactional(readOnly = true)
    public CertResponse getDataCert(GetDataCertRequest dataCertRequest) throws IOException, KeyStoreException, CertificateException, NoSuchAlgorithmException {
        try {
            String dataSubject = null;
            if (dataCertRequest.getIdCert() != null) {
                Optional<Certificate> dataKeystore = certificateRepository.findById(dataCertRequest.getIdCert());
                if (dataKeystore.isEmpty()) {
                    log.info("Dữ liệu chứng thư số server không tồn tại !!!");
                    return CertResponse.builder().build();
                }
                byte[] keyStoreData = dataKeystore.get().getKeystore();
                String alias = dataKeystore.get().getAlias();
                char[] password = dataKeystore.get().getPasswordKeystore().toCharArray();
                KeyStore keyStore = KeyStore.getInstance("PKCS12");
                keyStore.load(new ByteArrayInputStream(keyStoreData), password);
                X509Certificate certificate = (X509Certificate) keyStore.getCertificate(alias);
                dataSubject = certificate.getSubjectDN().getName();
            } else if (dataCertRequest.getDataSubject() != null) {
                dataSubject = String.join(",", dataCertRequest.getDataSubject());
            }

            if (dataSubject == null) {
                throw new RuntimeException("Dữ liệu chủ thể của chứng thư số trống !");
            }

            return setInformationCertificate(dataSubject);
        } catch (CustomException ce) {
            throw ce;
        } catch (Exception e) {
            log.error("Lấy thông tin chứng thư số thất bại {}", e);
            throw new RuntimeException("Lấy thông tin chứng thư số thất bại");
        }
    }

    /**
     * Hàm lấy thông tin chứng thư số từ subject của certificate
     *
     * @param subject thông tin chứng thư số
     * @return
     */
    public CertResponse setInformationCertificate(String subject) {
        var result = CertResponse.certResponseEmpty();
        try {
            LdapName ldapName = new LdapName(subject);
            for (var info : ldapName.getRdns()) {
                var title = StringUtils.hasText(info.getType()) ? info.getType().toUpperCase() : "";
                var value = Objects.nonNull(info.getValue()) ? String.valueOf(info.getValue()) : "";
                switch (title) {
                    case "CN":
                        result.setName(value);
                        break;
                    case "O":
                        result.setCompany(value);
                        break;
                    case "T":
                        result.setPosition(value);
                        break;
                    case "UID":
                        var splitIdentifier = value.split(":");
                        if (splitIdentifier.length == 2) {
                            var identifier = splitIdentifier[0].toUpperCase();
                            switch (identifier) {
                                case "MST":
                                    result.setMst(splitIdentifier[1]);
                                    break;
                                case "CMND":
                                    result.setCmnd(splitIdentifier[1]);
                                    break;
                                case "CCCD":
                                    result.setCccd(splitIdentifier[1]);
                                    break;
                            }
                        }
                        break;
                }
            }
        } catch (Exception e) {
            log.error("Đã có lỗi xảy ra trong quá trình lấy thông tin trong chứng thư số", e);
        }
        return result;
    }

    @Transactional(readOnly = true)
    public CertificateDto findCertById(Integer id, Authentication authentication) {

        try {
            String currentEmail = authentication.getName();

            OrganizationDTO organizationDTO = customerService.getOrganizationByCustomerEmail(currentEmail);

            CertificateDto certificateDataQuery;

            certificateDataQuery = certificateRepository.findById(id).map(CertificateDto::fromEntity)
                    .orElseThrow(() -> new CustomException(ResponseCode.CERT_NOT_FOUND));

            List<CertificateCustomersDto> dataRemove = new ArrayList<>();
            certificateDataQuery.getCustomers().forEach(c -> {
                if (!(organizationDTO.getId().equals(c.getId()))) {
                    dataRemove.add(c);
                }
            });
            certificateDataQuery.getCustomers().removeAll(dataRemove);

            return certificateDataQuery;
        } catch (CustomException ce) {
            throw ce;
        } catch (Exception e) {
            log.error("Lấy dữ liệu chứng thư số theo ID thất bại {}", e);
            throw new RuntimeException("Lấy dữ liệu chứng thư số theo ID thất bại");
        }
    }

    @Transactional(readOnly = true)
    public Page<CertificateDto> findAllCertKeystore(String subject, String serial_number, String status, int pageSize, int pageNumber, Authentication authentication) {
        try {

            String currentEmail = authentication.getName();

            OrganizationDTO organizationDTO = customerService.getOrganizationByCustomerEmail(currentEmail);

            List<CertificateDto> certificateDataQuery = certificateRepository.findByKeyStoreData(subject, status, serial_number, organizationDTO.getId())
                    .stream().map(CertificateDto::fromEntity).collect(Collectors.toList());


            // lấy dữ liệu cert loại bỏ người nếu không cùng trong tổ chức hiện tại và tổ chức con
            certificateDataQuery.forEach(certificateData -> {
                List<CertificateCustomersDto> dataRemove = new ArrayList<>();
                certificateData.getCustomers().forEach(c -> {
                    if (!(organizationDTO.getId().equals(c.getOrganization_id()))) {
                        dataRemove.add(c);
                    }
                });
                certificateData.getCustomers().removeAll(dataRemove);
            });
            Pageable pageRequest = PageRequest.of(pageNumber, pageSize);
            int start = (int) pageRequest.getOffset();
            int end = Math.min((start + pageRequest.getPageSize()), certificateDataQuery.size());
            List<CertificateDto> pageContent = new ArrayList<>(certificateDataQuery.stream()
                    .sorted(Comparator.comparing(CertificateDto::getId).reversed()).collect(Collectors.toList())).subList(start, end);
            return new PageImpl<>(pageContent, pageRequest, certificateDataQuery.size());
        } catch (CustomException ce) {
            throw ce;
        } catch (Exception e) {
            log.error("Lấy dữ liệu chứng thư số theo ID thất bại {}", e);
            throw new RuntimeException("Lấy dữ liệu chứng thư số theo ID thất bại");
        }
    }


}
