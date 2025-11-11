package com.ec.contract.service;

import com.ec.contract.model.dto.CertResponse;
import com.ec.contract.model.dto.keystoreDTO.CertificateDtoRequest;
import com.ec.contract.model.dto.keystoreDTO.GetDataCertRequest;
import com.ec.contract.repository.CertificateRepository;
import com.ec.contract.repository.RecipientRepository;
import com.ec.library.exception.CustomException;
import com.ec.library.exception.ResponseCode;
import com.ec.library.response.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SignService {

    private final RecipientRepository recipientRepository;
    private final CertificateRepository certificateRepository;
    private final CertService certService;

    // ký cert mềm
    @Transactional
    public Response<?> SignKeystore(int recipientId, CertificateDtoRequest certificateDtoRequest) {
        Date dateActionSign = new Date();
        log.info("start sign keyStore for recipient: {}", recipientId);
        final var recipient = recipientRepository.findById(recipientId).orElse(null);
        CertResponse certResponse = new CertResponse();
        try {
            certResponse = certService.getDataCert(new GetDataCertRequest(certificateDtoRequest.getCertificate_id(), null));
        } catch (Exception e) {
            log.error("lấy dữ liệu chứng thư số thất bại ", e);
            return Response.error(null, "Lấy thông tin chữ ký của chủ thể thất bại!");
        }

        if (recipient == null) {
            return Response.error(ResponseCode.RECIPIENT_NOT_FOUND);
        }

        String taxCode = recipient.getCardId();

        if (taxCode == null || taxCode != null
                && !(certResponse.getMst().equals(taxCode))
                && !(certResponse.getCccd().equals(taxCode))
                && !(certResponse.getCmnd().equals(taxCode))) {
            return Response.error(null, "MST/CMT/CCCD của Chứng thư số không trùng khớp với thông tin MST/CMT/CCCD người tạo đã khai báo.");
        }

        //Get token

        try {
            var contractId = recipient.getParticipant().getContractId();
            final var docCollection = documentRepository.findAllByContractIdAndStatusOrderByIdDesc(
                    contractId, BaseStatus.ACTIVE.ordinal()
            );

            certificateDtoRequest.setIsTimestamp("false");
            var contract = contractRepository.findById(contractId).orElse(null);
            if (contract != null && contract.getCecaPush() == 1) {
                certificateDtoRequest.setIsTimestamp("true");
            }

            //Lấy thông tin file hợp đồng
            final var docOptional = docCollection.stream().filter(
                    document -> document.getType() == DocumentType.FINALLY
            ).findFirst();

            if (docOptional.isEmpty()) {
                return MessageDto.builder()
                        .success(false)
                        .message("Can't find file contract")
                        .build();
            }

            if (docOptional.isPresent()) {
                final var doc = docOptional.get();
                final var presignedUrl = fileService.getPresignedObjectUrl(
                        doc.getBucket(),
                        doc.getPath()
                );

                //Lấy thông tin ô ký số
                final var fieldOptinal = fieldRepository.findFirstByRecipientIdAndType(recipientId, FieldType.DIGITAL_SIGN);
                boolean recipientArchiver = recipient.getRole() != null && recipient.getRole().equals(RecipientRole.ARCHIVER);
                if (fieldOptinal.isEmpty() && !recipientArchiver) {
                    return MessageDto.builder()
                            .success(false)
                            .message("Can't find sign field")
                            .build();
                }
                Optional<Field> fieldSign = Optional.empty();
                if (certificateDtoRequest.getField().getId() != null) {
                    fieldSign = fieldRepository.findById(certificateDtoRequest.getField().getId());
                }


                if (certificateDtoRequest.getType() != null && certificateDtoRequest.getType().equals(FieldType.DIGITAL_SIGN.getDbVal())) {
                    certificateDtoRequest.setImageBase64(
                            genImageSignature(SignatureDtoRequest.builder()
                                    .signType(SignType.CERT.getDbVal())
                                    .signBy(certResponse.getName())
                                    .taxCodeOrIdentification(taxCode)
                                    .typeImageSignature(fieldSign.map(Field::getTypeImageSignature).orElse(null))
                                    .base64Image(certificateDtoRequest.getImageBase64())
                                    .dateActionSign(dateActionSign)
                                    .build())
                    );
                }

                //Gọi API Ký File Keystore và replace file hợp đồng sau khi ký

//                var field = (certificateDtoRequest.getType() != null
//                        && certificateDtoRequest.getType().equals(FieldType.DIGITAL_SIGN.getDbVal())
//                        && fieldSign.isPresent()) ?
//                        fieldSign.get()
//                        : modelMapper.map(certificateDtoRequest.getField(), Field.class);

                var keyStoreCoordinateOptional = convertCoordinateToPKI(modelMapper.map(certificateDtoRequest.getField(), Field.class), presignedUrl);
                var dataField = keyStoreCoordinateOptional.get();
                Field fieldConvertCoordinate = modelMapper.map(dataField, Field.class);
                String signMessage = replaceFileAfterSignKeystore(fieldConvertCoordinate, contractId, presignedUrl,
                        doc.getFilename(), certificateDtoRequest);
                if (signMessage.equals("success")) {
                    log.info("sign contract success");

                    if (contract != null) {
                        customerService.decreaseNumberOfTimestamp(contract.getOrganizationId());
                    }
                    // chuyen trang thai cua feild
                    if (fieldSign.isPresent()) {
                        fieldSign.get().setStatus(BaseStatus.ACTIVE);
                        fieldRepository.save(fieldSign.get());
                    }
                    log.info("Sign Keystore for recipient: {} successfully!", recipientId);
                    recipientService.updateStartSignAndSignEnd(recipientId, dateActionSign);
                    return MessageDto.builder()
                            .success(true)
                            .message("successful")
                            .build();

                }
                backupFileSignFail(recipientId);
                return MessageDto.builder()
                        .success(false)
                        .message(signMessage)
                        .build();

            }


        } catch (Exception e) {
            log.error("Đã có lỗi xảy ra trong quá trình ký chứng thư số {}", e);
        }
        backupFileSignFail(recipientId);
        log.info("Sign keystore for recipient: {} has error!", recipientId);
        return MessageDto.builder()
                .success(false)
                .message("Unexpected error")
                .build();
    }

}
