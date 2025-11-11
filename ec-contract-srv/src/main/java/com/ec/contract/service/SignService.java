package com.ec.contract.service;

import com.ec.contract.constant.*;
import com.ec.contract.model.dto.CertResponse;
import com.ec.contract.model.dto.CoordinateDto;
import com.ec.contract.model.dto.ImageGenerateDto;
import com.ec.contract.model.dto.keystoreDTO.CertificateDtoRequest;
import com.ec.contract.model.dto.keystoreDTO.GetDataCertRequest;
import com.ec.contract.model.dto.signatureDto.SignatureDtoRequest;
import com.ec.contract.model.entity.Field;
import com.ec.contract.repository.*;
import com.ec.contract.util.ImageUtils;
import com.ec.contract.util.StringUtil;
import com.ec.library.exception.CustomException;
import com.ec.library.exception.ResponseCode;
import com.ec.library.response.Response;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
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
    private final FieldService fieldService;
    private final FieldRepository fieldRepository;
    private final ModelMapper modelMapper;


    private static final String spaceGenImage = "              ";
    private static final double cell_text_ratio = 0.7;
    private static final double cell_image_ratio = 0.3;

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

            //Lấy thông tin file hợp đồng
            final var docOptional = docCollection.stream().filter(
                    document -> Objects.equals(document.getType(), DocumentType.GOC.getDbVal())
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

    /**
     *
     * @param field
     * @param presignedUrl
     * @return
     *
     *  x giữ nguyên
    - y = chiều cao của trang chứa chữ ký - (toạ độ y truyền lên - pageHeight) - chiều cao ô ký
    - w = chiều rộng ô ký
    - h = chiều cao ô ký
    (pageHeight là tổng chiều cao trang thứ nhất đến trang chứa chữ ký trừ đi 1;
    ví dụ: trang chứa ô ký là trang 5 thì currentHeight sẽ là tổng chiều cao từ trang thứ nhất đến trang thứ 4
    )
     */
    public Optional<CoordinateDto> convertCoordinateToPKI(Field field, String presignedUrl) {
        var pageIndex = field.getPage();
        double toX = field.getBoxX();
        double toY = field.getBoxY();
        double toW = field.getBoxW();
        double toH = field.getBoxH();

        try {
            var pdfDoc = new PdfDocument(new PdfReader(new URL(presignedUrl).openStream()));
            int rotation = pdfDoc.getPage(pageIndex).getRotation();

            if (rotation == 0) {
                // FE cong chieu cao tat ca cac trang vao toa do y (y_n = page(0).h + page(1).h + .... + page(n).h)
                for (int i = 1 ;i < pageIndex; i++) {
                    toY = (float) (toY -  Math.floor(pdfDoc.getPage(i).getPageSize().getHeight()));
                }

                // convert top left -> bottom left
//                toY = (float) (Math.floor(pdfDoc.getPage(pageIndex).getPageSize().getHeight()) - toY - toH + (pageIndex) * 6);
                toY = (float) (Math.floor(pdfDoc.getPage(pageIndex).getPageSize().getHeight()) - toY - toH + (pageIndex) * 5.0);
            } else {
                for (int i = 1; i < pageIndex; i++) {
                    toY = toY - (float) Math.floor(pdfDoc.getPage(i).getPageSize().getWidth());

//                    toY = (float) (toY -  Math.floor(pdfDoc.getPage(i).getPageSize().getHeight()));
                }
                toY = toY - 10;
                double tmp = toX;
                toX = toY;
                toY = tmp;

                tmp = toW;
                toW = toH;
                toH = tmp;
            }


            //chuyển độ tọa độ và làm tròn
            int toX1 = (int) Math.round(toX);
            int toY1 = (int) Math.round(toY);
            int toX2 = (int) Math.round(toW);
            int toY2 = (int) Math.round(toH);

            return Optional.of(
                    CoordinateDto.builder()
                            .coordinateX(toX1)
                            .coordinateY(toY1)
                            .width(toX2)
                            .height(toY2)
                            .page(pageIndex)
                            .build()
            );
        }catch (Exception e) {
            log.error(String.format("can't load pdf sign sim pki at \"%s\"", presignedUrl), e);
        }

        return Optional.empty();
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
