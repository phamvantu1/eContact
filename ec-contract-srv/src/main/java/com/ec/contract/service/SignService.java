package com.ec.contract.service;

import com.ec.contract.constant.*;
import com.ec.contract.model.dto.CertResponse;
import com.ec.contract.model.dto.CoordinateDto;
import com.ec.contract.model.dto.ImageGenerateDto;
import com.ec.contract.model.dto.keystoreDTO.CertificateDtoRequest;
import com.ec.contract.model.dto.keystoreDTO.GetDataCertRequest;
import com.ec.contract.model.dto.signatureDto.SignatureDtoRequest;
import com.ec.contract.model.entity.Field;
import com.ec.contract.model.entity.keystoreEntity.Certificate;
import com.ec.contract.repository.*;
import com.ec.contract.service.signatureContainer.MyExternalSignatureContainer;
import com.ec.contract.util.ImageUtils;
import com.ec.contract.util.StringUtil;
import com.ec.library.exception.ResponseCode;
import com.ec.library.response.Response;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.*;
import com.itextpdf.signatures.ExternalBlankSignatureContainer;
import com.itextpdf.signatures.IExternalSignatureContainer;
import com.itextpdf.signatures.PdfSignatureAppearance;
import com.itextpdf.signatures.PdfSigner;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SignService {

    private final RecipientRepository recipientRepository;
    private final CertificateRepository certificateRepository;
    private final CertService certService;
    private final ContractRepository contractRepository;
    private final DocumentRepository documentRepository;
    private final DocumentService documentService;
    private final FieldRepository fieldRepository;
    private final ModelMapper modelMapper;

    private static final String spaceGenImage = "              ";
    private static final double cell_text_ratio = 0.7;
    private static final double cell_image_ratio = 0.3;
    private final RecipientService recipientService;

    // ký cert mềm
    @Transactional
    public Response<?> SignKeystore(int recipientId, CertificateDtoRequest certificateDtoRequest) {
        Date dateActionSign = new Date();
        log.info("start sign keyStore for recipient: {}", recipientId);
        final var recipient = recipientRepository.findById(recipientId).orElse(null);
        CertResponse certResponse = new CertResponse();
        try {
            certResponse = certService.getDataCert(new GetDataCertRequest(certificateDtoRequest.getCertId(), null));
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

            //Lấy thông tin file hợp đồng
            final var docOptional = docCollection.stream().filter(
                    document -> Objects.equals(document.getType(), DocumentType.FINALLY.getDbVal())
            ).findFirst();

            if (docOptional.isEmpty()) {
                return Response.error(ResponseCode.DOCUMENT_NOT_FOUND);
            }

            final var doc = docOptional.get();
            Map<String,String> mapPresignedUrl = documentService.getPresignedUrl(doc.getId());

            final var presignedUrl = mapPresignedUrl.get("message");

            //Lấy thông tin ô ký số
            final var fieldOptinal = fieldRepository.findFirstByRecipientIdAndType(recipientId, FieldType.DIGITAL_SIGN.getDbVal());

            if (fieldOptinal.isEmpty()) {
                return Response.error(ResponseCode.FIELD_SIGN_NOT_FOUND);
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

            var keyStoreCoordinateOptional = convertCoordinateToPKI(modelMapper.map(certificateDtoRequest.getField(), Field.class), presignedUrl);
            var dataField = keyStoreCoordinateOptional.get();
            Field fieldConvertCoordinate = modelMapper.map(dataField, Field.class);

            String signMessage = replaceFileAfterSignKeystore(fieldConvertCoordinate, contractId, presignedUrl,
                    doc.getFileName(), certificateDtoRequest);

            if (signMessage.equals("success")) {
                log.info("sign contract success");

                // chuyen trang thai cua feild
                if (fieldSign.isPresent()) {
                    fieldSign.get().setStatus(BaseStatus.ACTIVE.ordinal());
                    fieldRepository.save(fieldSign.get());
                }

                log.info("Sign Keystore for recipient: {} successfully!", recipientId);

                recipientService.updateStartSignAndSignEnd(recipientId, LocalDateTime.now());

                return Response.success("Ký chứng thư số thành công!");

            }

//            backupFileSignFail(recipientId);
//            return MessageDto.builder()
//                    .success(false)
//                    .message(signMessage)
//                    .build();


        } catch (Exception e) {
            log.error("Đã có lỗi xảy ra trong quá trình ký chứng thư số {}", e);
        }

//        backupFileSignFail(recipientId);

        log.info("Sign keystore for recipient: {} has error!", recipientId);
        return Response.error(null, "Đã có lỗi xảy ra trong quá trình ký chứng thư số!");
    }

    // kí file cert mềm
    private String replaceFileAfterSignKeystore(Field field, int contractId, String presignedUrl, String fileName, CertificateDtoRequest certificateDtoRequest) {
        log.info("\n \n ----- BAT DAU THUC HIEN KY CHUNG THU SO ----- \n \n - field DATA: "  + " ---- boxX : "+ field.getBoxX() + " ---- boxY : " + field.getBoxY()+ "\n \n");
        final var tempFolder = "./tmp/" + UUID.randomUUID();

        InputStream is = null;
        FileOutputStream fos = null;

        try {
            // make a directory
            FileUtils.forceMkdir(new File(tempFolder));

            Optional<Certificate> dataKeystore = certificateRepository.findById(certificateDtoRequest.getCertId());
            if (dataKeystore.isEmpty()) {
                log.info("Dữ liệu Certificate rỗng id :{}", certificateDtoRequest.getCertId());
                return "Đã có lỗi xảy ra trong quá trình ký";
            }
            List<String> email = new ArrayList<>();

            dataKeystore.get().getCertificateCustomers().forEach(a -> {
                email.add(a.getEmail());
            });



            if ( (email.isEmpty()) || (!( email.contains(certificateDtoRequest.getEmail()))) ) {
                return "Bạn không được cấp quyền sử dụng với chứng thư số này";
            }
            if (dataKeystore.get().getKeystoreDateEnd().compareTo(new Date()) < 0) {
                return "Certificate đã hết hạn";
            }
//            if (!(dataKeystore.get().getStatus().equals("1"))) {
//                return "Certificate đã bị vô hiệu hóa";
//            }

            byte[] keyStoreData = dataKeystore.get().getKeystore();
            String alias = dataKeystore.get().getAlias();
            char[] password = dataKeystore.get().getPasswordKeystore().toCharArray();

            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            keyStore.load(new ByteArrayInputStream(keyStoreData), password);

            PrivateKey pk = null;
            try {
                pk = (PrivateKey) keyStore.getKey(alias, password);
            } catch (Exception e) {
                log.error("Lỗi file dữ liệu keystore {}",e);
                return "false";
            }

            java.security.cert.Certificate[] chain = keyStore.getCertificateChain(alias);

            byte[] bytes = IOUtils.toByteArray(
                    new URL(presignedUrl));
            var pdfDoc = new PdfDocument(new PdfReader(new ByteArrayInputStream(bytes)));
            int rotation = pdfDoc.getPage(field.getPage()).getRotation();
            String fieldName = UUID.randomUUID().toString().toUpperCase(Locale.ROOT).replace("-", "");
            ImageData imageData = ImageDataFactory.create(Base64.getDecoder().decode(certificateDtoRequest.getImageBase64()));
            if (rotation == 0 && certificateDtoRequest.getType().equals(FieldType.DIGITAL_SIGN.getDbVal())) {
                double toYOld = Double.sum(field.getBoxY(), field.getBoxH());
                double newHeight = (imageData.getHeight() / imageData.getWidth()) * field.getBoxW();
                field.setBoxH(newHeight);
                field.setBoxY(toYOld - newHeight);
            }

            changDbFieldName(fieldName, contractId);

            float x = (float) field.getBoxX().floatValue();

            log.info("------boxX this box X after convert : " + field.getBoxX());
            log.info("this box Y after convert : " + field.getBoxY());

            InputStream inputStream = this.emptySignature(new ByteArrayInputStream(bytes), fieldName, chain,field.getBoxX().floatValue()
                    , field.getBoxY().floatValue(), field.getBoxW().floatValue(), field.getBoxH().floatValue(), Integer.valueOf(field.getPage()), certificateDtoRequest.getImageBase64());
            byte[] datafileSign = this.createSignature(inputStream, pk, fieldName, chain, certificateDtoRequest.getIsTimestamp());
            try {
                pdfDoc.close();
                inputStream.close();
            } catch (Exception e) {
                log.error("Lỗi close thư viện");
            }

            is = new ByteArrayInputStream(datafileSign);
            fos = new FileOutputStream(tempFolder + "/" + fileName);


            // download file
            int inByte;
            while ((inByte = is.read()) != -1) {
                fos.write(inByte);
            }

            // get document
            final var documentCollection = documentRepository
                    .findAllByContractIdAndStatusOrderByIdDesc(
                            contractId, BaseStatus.ACTIVE.ordinal());
            final var docOptional = documentCollection.stream().filter(
                    document -> document.getType() == DocumentType.FINALLY.getDbVal()).findFirst();

            if (docOptional.isPresent()) {
                final var doc = docOptional.get();

                final var uploadFileDtoOptional = documentService
                        .replace(tempFolder + "/" + fileName, doc);

                if (uploadFileDtoOptional.equals("success")) {
                    return "success";
                } else {
                    return "false";
                }
            }

        }  catch (Exception e) {
            log.error("Đã có lỗi xảy ra trong quá trình ký chứng thư số", e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException ex) {
                    log.error("can't close input stream", ex);
                }
            }

            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException ex) {
                    log.error("can't close file output stream");
                }
            }

            try {
                FileUtils.forceDelete(new File(tempFolder));
            } catch (IOException ex) {
                log.error("can't delete directory, path = {}", tempFolder);
            }
        }

        return "false";
    }

    public byte[] createSignature(InputStream inputStream, PrivateKey privateKey, String fieldName, java.security.cert.Certificate[] chain, String isTimestamp) {
        try {
            PdfReader reader = new PdfReader(inputStream);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            PdfSigner signer = new PdfSigner(reader, byteArrayOutputStream, new StampingProperties());

            IExternalSignatureContainer external = new MyExternalSignatureContainer(privateKey, chain);

            // Signs a PDF where space was already reserved. The field must cover the whole document.
            signer.signDeferred(signer.getDocument(), fieldName, byteArrayOutputStream, external);
            byte[] dataSignSuccess = byteArrayOutputStream.toByteArray();
            byteArrayOutputStream.close();
            return dataSignSuccess;
        } catch (Exception e) {
            log.error("lỗi tạo tại hàm createSignature {}",e);
            throw new RuntimeException(e.getMessage());
        }
    }

    public InputStream emptySignature(InputStream inputStream,
                                      String fieldName,
                                      java.security.cert.Certificate[] chain,
                                      float toX,
                                      float toY,
                                      float toW,
                                      float toH,
                                      Integer pageNumber,
                                      String imageBase64) {
        try {
            log.info("----- toa do ky so before conversion x: {} , y: {} , w: {} , h: {} , pageNumber: {}",
                    toX, toY, toW, toH, pageNumber);

            byte[] image = Base64.getDecoder().decode(imageBase64);

            PdfReader reader = new PdfReader(inputStream);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

            StampingProperties stampingProperties = new StampingProperties().useAppendMode();
            PdfSigner signer = new PdfSigner(reader, byteArrayOutputStream, stampingProperties);

            PdfSignatureAppearance appearance = signer.getSignatureAppearance();

            // --- Fix tọa độ Y ---
            PdfPage page = signer.getDocument().getPage(pageNumber);
            float pageHeight = page.getPageSize().getHeight();
            float correctedY = pageHeight - toY - toH; // convert top-left (FE) -> bottom-left (PDF)
            log.info("Corrected Y for PDF bottom-left coordinates: {}", correctedY);

            appearance.setPageRect(new Rectangle(toX, correctedY, toW, toH))
                    .setPageNumber(pageNumber)
                    .setSignatureGraphic(ImageDataFactory.create(image))
                    .setRenderingMode(PdfSignatureAppearance.RenderingMode.GRAPHIC)
                    .setCertificate(chain[0]);

            signer.setFieldName(fieldName);

            // Tạo placeholder signature trống
            IExternalSignatureContainer external = new ExternalBlankSignatureContainer(
                    PdfName.Adobe_PPKLite, PdfName.Adbe_pkcs7_detached);

            // Kích thước placeholder (có thể tăng nếu cần)
            signer.signExternalContainer(external, 200000);

            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
            byteArrayOutputStream.close();
            return byteArrayInputStream;

        } catch (Exception e) {
            log.error("Lỗi tạo signature trống tại hàm emptySignature", e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }


    private void changDbFieldName(String fieldName, Integer fieldId) {
        Optional<Field> fieldOptional = fieldRepository.findById(fieldId);
        if (fieldOptional.isPresent()) {
            fieldOptional.get().setFieldName(fieldName);
            fieldRepository.save(fieldOptional.get());
        }
    }

    /**
     * Hàm chuyển đổi tọa độ từ hệ thống sang hệ thống
     *
     */
    public Optional<CoordinateDto> convertCoordinateToPKI(Field field, String presignedUrl) {

        log.info("_------pa this box X before convert : {}", field.getBoxX());
        log.info("---tiu this box Y before convert : {}", field.getBoxY());
        log.info("+========convertCoordinateToPKI - field: {}", field);

        int pageIndex = field.getPage();
        double toX = field.getBoxX();
        double toY = field.getBoxY();
        double toW = field.getBoxW();
        double toH = field.getBoxH();

        int fx = (int) Math.round(toX);
        int fy = (int) Math.round(toY);
        int fw = (int) Math.round(toW);
        int fh = (int) Math.round(toH);

        log.info("xion chao this box X after convert : {}", fx);
        log.info("heh ohe this box Y after convert : {}", fy);

        return Optional.of(
                CoordinateDto.builder()
                        .boxX(fx)
                        .boxY(fy)
                        .boxW(fw)
                        .boxH(fh)
                        .page(pageIndex)
                        .build()
        );
    }

    /**
     * Hàm gen ảnh chữ ký
     * @param signatureDtoRequest dữ liệu request tạo ảnh chữ ký
     * @return
     */
    public String genImageSignature(SignatureDtoRequest signatureDtoRequest) {

        ImageGenerateDto imageGenerateDto = new ImageGenerateDto();
        if (!(StringUtils.hasText(signatureDtoRequest.getSignBy()))) {
            signatureDtoRequest.setSignBy(" ");
        }

        if (!(StringUtils.hasText(signatureDtoRequest.getTaxCodeOrIdentification()))) {
            signatureDtoRequest.setTaxCodeOrIdentification(" ");
        }

        String base64Signature = null;
        String base64Icon = null;
        try {
            if (StringUtils.hasText(signatureDtoRequest.getBase64Image())) {
                base64Icon = signatureDtoRequest.getBase64Image();
            } else {
                base64Icon = Constants.anhPngIconGanVaoChuKy;
            }

            String[] splitNameUserCheckLength = signatureDtoRequest.getSignBy()
                    .trim()
                    .replaceAll("\\s+", " ")
                    .split(" ");
            List<String> lines = new ArrayList<>();
            Date actionDate = signatureDtoRequest.getDateActionSign() != null ? signatureDtoRequest.getDateActionSign() : new Date();

            SimpleDateFormat isoFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            isoFormat.setTimeZone(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));

            String date = isoFormat.format(actionDate) + " +07:00";;

            if (signatureDtoRequest.getSignType().equals(SignType.HSM.getDbVal())
                    || signatureDtoRequest.getSignType().equals(SignType.USB_TOKEN.getDbVal())
                    || signatureDtoRequest.getSignType().equals(SignType.CERT.getDbVal())
//                    || signatureDtoRequest.getSignType().equals(SignType.REMOTE_SIGNING.getDbVal())
            ) {
                String textFirst = "Đã thực hiện ký số".concat(spaceGenImage);
                lines.add(textFirst);
                var signBy = StringUtil.combineCharacters("Ký bởi:", splitNameUserCheckLength, textFirst.length() - 5);
                lines.addAll(signBy);
                lines.add("MST/CMT/CCCD: ".concat(signatureDtoRequest.getTaxCodeOrIdentification()));
                lines.add("Ngày ký: ".concat(date));
            } else if (signatureDtoRequest.getSignType().equals(SignType.IMAGE_AND_OTP.getDbVal())) {
                String textFirst = "Đã xác thực OTP".concat(spaceGenImage).concat("   ");
                lines.add(textFirst);
                var signBy = StringUtil.combineCharacters("Ký bởi:", splitNameUserCheckLength, textFirst.length() - 5);
                lines.addAll(signBy);
                lines.add("Số điện thoại: ".concat(signatureDtoRequest.getTaxCodeOrIdentification()));
                lines.add("Ngày ký: ".concat(date));
            } else if (signatureDtoRequest.getSignType().equals(SignType.EKYC.getDbVal())) {
                String textFirst = "Đã xác thực eKYC".concat(spaceGenImage).concat("  ");
                lines.add(textFirst);
                var signBy = StringUtil.combineCharacters("Ký bởi:", splitNameUserCheckLength, textFirst.length() - 5);
                lines.addAll(signBy);
                lines.add("MST/CMT/CCCD/HC: ".concat(signatureDtoRequest.getTaxCodeOrIdentification()));
                lines.add("Ngày ký: ".concat(date));
            } else if (signatureDtoRequest.getSignType().equals(SignType.SIM_PKI.getDbVal())) {
                String textFirst = "Đã thực hiện ký số".concat(spaceGenImage);
                lines.add(textFirst);
                var signBy = StringUtil.combineCharacters("Ký bởi:", splitNameUserCheckLength, textFirst.length() - 5);
                lines.addAll(signBy);
                if (signatureDtoRequest.getHiddenPhone() == null || !signatureDtoRequest.getHiddenPhone()){
                    lines.add("Số điện thoại: ".concat(signatureDtoRequest.getTaxCodeOrIdentification()));
                }
                lines.add("Ngày ký: ".concat(date));
            }else if (signatureDtoRequest.getSignType().equals(SignType.REMOTE_SIGNING.getDbVal())){
                String textFirst = "Đã thực hiện ký số".concat(spaceGenImage);
                lines.add(textFirst);
                var signBy = StringUtil.combineCharacters("Ký bởi:", splitNameUserCheckLength, textFirst.length() - 5);
                lines.addAll(signBy);
                lines.add("MST/CMT/CCCD/HC: ".concat(signatureDtoRequest.getTaxCodeOrIdentification()));
                lines.add("Ngày ký: ".concat(date));
            }


            log.info("Line Thông tin ảnh chữ ký :" + lines);
            // Tạo font

            Font boldFont = new Font("Roboto", Font.BOLD, 30);
            Font plainFont = new Font("Roboto", Font.PLAIN, 30);

            // Tính toán kích thước của hình ảnh
            BufferedImage tempImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
            Graphics2D tempGraphics = tempImage.createGraphics();
            tempGraphics.setFont(boldFont);
            FontMetrics fontMetrics = tempGraphics.getFontMetrics();
            int width = 0;
            for (String line : lines) {
                var replaceString = line.replaceAll(" ","_");
                int lineWidth = fontMetrics.stringWidth(replaceString);
                if (lineWidth > width) {
                    width = lineWidth;
                }
            }
            int height = fontMetrics.getHeight() * lines.size();
            tempGraphics.dispose();

            // Tạo hình ảnh
            BufferedImage image = null;
//            if (signatureDtoRequest.getSignType().equals(SignType.IMAGE_AND_OTP.getDbVal())) {
//                image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
//            } else {
            image = new BufferedImage(width, height + (int) (0.5 * fontMetrics.getHeight() * (lines.size() - 1)), BufferedImage.TYPE_INT_ARGB);
//            }
            Graphics2D graphics = image.createGraphics();
            graphics.setColor(Color.decode("#2A4E78")); // Màu chữ
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                graphics.setFont(i == 0 ? boldFont : plainFont);
//                if (signatureDtoRequest.getSignType().equals(SignType.IMAGE_AND_OTP.getDbVal())) {
//                    graphics.drawString(line, 0, fontMetrics.getAscent() + (fontMetrics.getHeight() * i));
//                } else {
                graphics.drawString(line, 0, fontMetrics.getAscent() + (fontMetrics.getHeight() * i) + (int) (0.5 * fontMetrics.getHeight() * i));
//                }
            }
            graphics.dispose();
            ByteArrayOutputStream dataSignatureOutputStream = new ByteArrayOutputStream();
            // Ghi hình ảnh vào file
            ImageIO.write(image, "png", dataSignatureOutputStream);
            dataSignatureOutputStream.flush();
            byte[] imageInByte = dataSignatureOutputStream.toByteArray();
            imageGenerateDto.setImageSignature(Base64.getEncoder().encodeToString(imageInByte));
            dataSignatureOutputStream.close();

            // ghép 2 ảnh
//            boolean condition = true;
            byte[] dataIcon = Base64.getDecoder().decode(base64Icon);
            imageGenerateDto.setImageIcon(base64Icon);
            BufferedImage icon = ImageUtils.getRotatedImage(dataIcon);
            BufferedImage signatureImage = ImageIO.read(new ByteArrayInputStream(imageInByte));

            // Get the dimensions of the second image
            int width1 = icon.getWidth();
            int height1 = icon.getHeight();
            int width2 = signatureImage.getWidth();
            int height2 = signatureImage.getHeight();

            // set chiều cao ảnh 2 bằng ảnh 1
//            int newHeight1 = height2;

            // set lại tạm thời chiều cao như cũ
            int newHeight1 = height1;

            // Calculate the new width while maintaining the aspect ratio
            double aspectRatio = (double) width1 / height1;
            int newWidth1 = (int) (newHeight1 * aspectRatio);

            // Tính lại chiều rộng ảnh ghép vào sẽ bằng 30% ảnh chữ ký
            long maxSize = Math.round((float) width2 / (cell_text_ratio));
            long checkSizeImage1 = Math.round((float) maxSize * (cell_image_ratio));
            newWidth1 = Long.valueOf(checkSizeImage1).intValue();
            newHeight1 = (int) (newWidth1 / aspectRatio);


            if(signatureDtoRequest.getSignType().equals(SignType.SIM_PKI.getDbVal()) && signatureDtoRequest.getTypeImageSignature().equals(2)){
                double resizeFactor = 0.7;  // Giảm kích thước ảnh con dấu xuống 50%
                ByteArrayInputStream bais = new ByteArrayInputStream(dataIcon);
                BufferedImage bufferedImage = ImageIO.read(bais);
                width1 = bufferedImage.getWidth();
                height1 = bufferedImage.getHeight();
                // Bước 1: Cắt ảnh vuông (chiều rộng = chiều cao)
                if (width1 > height1 * 1.05) { // Chiều rộng lớn hơn so với chiều cao
                    int newWidth = Math.min((int) (height1 * 1.05), width1); // Giới hạn chiều rộng không vượt quá ảnh gốc
                    icon = bufferedImage.getSubimage(0, 0, newWidth, height1); // Cắt từ góc trên bên trái
                    width1 = newWidth; // Cập nhật chiều rộng sau khi cắt
                } else {
                    icon = bufferedImage; // Không cần cắt
                }
                newWidth1 = (int) (width1 * resizeFactor);
                newHeight1 = (int) (height1 * resizeFactor);

            }


            // Step 4: Resize image 1 without distortion
            Image tmp = icon.getScaledInstance(newWidth1, newHeight1, Image.SCALE_SMOOTH);
            BufferedImage resizedImg1 = new BufferedImage(newWidth1, newHeight1, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2ds = resizedImg1.createGraphics();
            g2ds.drawImage(tmp, 0, 0, null);
            g2ds.dispose();

            // Concatenate the images
            int yPosImg1 = (Math.max(newHeight1, height2) - newHeight1) / 2;
            BufferedImage combined = new BufferedImage(newWidth1 + width2, Math.max(newHeight1, height2), BufferedImage.TYPE_INT_ARGB);
            g2ds = combined.createGraphics();

            // trường hợp icon muốn đảo chiều
            if (!(StringUtils.hasText(signatureDtoRequest.getIconPosition()))
                    || signatureDtoRequest.getIconPosition().equalsIgnoreCase("left")
                    || !(signatureDtoRequest.getIconPosition().equalsIgnoreCase("right"))) {
//                g2ds.drawImage(resizedImg1, 0, (newHeight1 <= height1) ? yPosImg1 : 0, null);
                g2ds.drawImage(resizedImg1, 0, 0, null);
                g2ds.drawImage(signatureImage, newWidth1, 0, null);
            } else {
                g2ds.drawImage(signatureImage, 0, 0, null);
                g2ds.drawImage(resizedImg1, width2, 0, null);
//                g2ds.drawImage(resizedImg1, width2, (newHeight1 <= height1) ? yPosImg1 : 0, null);
            }

            g2ds.dispose();

            // lưu ảnh đã tạo thành công vào ByteArrayOutputStream
            ByteArrayOutputStream dataImage = new ByteArrayOutputStream();
            ImageIO.write(combined, "PNG", dataImage);
            dataImage.flush();
            byte[] imageInBytes = dataImage.toByteArray();
            dataImage.close();
            log.info("Resized Image 1 - Width: " + resizedImg1.getWidth() + ", Height: " + resizedImg1.getHeight());
            if (signatureDtoRequest.getSignType().equals(SignType.SIM_PKI.getDbVal()) ) {
                ByteArrayOutputStream iconNew = new ByteArrayOutputStream();
                ImageIO.write(resizedImg1, "PNG", iconNew);
                byte[] byteIconResize = iconNew.toByteArray();
                String base64IconResize = Base64.getEncoder().encodeToString(byteIconResize);
                imageGenerateDto.setImageIcon(base64IconResize);
                // Log the size of the resized image in bytes, KB, and MB
                long imageSizeInBytes = byteIconResize.length;
                double imageSizeInKB = imageSizeInBytes / 1024.0;
                log.info("Resized Image Size: " + imageSizeInKB + " KB");
            }

            // thêm tích xanh vào nếu ImageIcon truyền xuống tồn tại

            if (signatureDtoRequest.getTypeImageSignature() == null
                    || signatureDtoRequest.getTypeImageSignature().equals(3)) {
                base64Signature = Base64.getEncoder().encodeToString(imageInBytes);
            } else if (signatureDtoRequest.getTypeImageSignature().equals(2)) {
                base64Signature = imageGenerateDto.getImageIcon();
            } else if (signatureDtoRequest.getTypeImageSignature().equals(1)) {
                base64Signature = imageGenerateDto.getImageSignature();
            }
        } catch (Exception e) {
            log.error("Lỗi tạo ảnh chữ ký {}",e);
            throw new RuntimeException("đã có lỗi xảy ra trong quá trình tạo ảnh chữ ký : " + e.getMessage());
        }
        return base64Signature;
    }

}
