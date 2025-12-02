package com.ec.contract.service;

import com.ec.contract.constant.BaseStatus;
import com.ec.contract.constant.DocumentType;
import com.ec.contract.constant.FieldType;
import com.ec.contract.mapper.ContractMapper;
import com.ec.contract.model.dto.response.ContractResponseDTO;
import com.ec.contract.model.entity.Field;
import com.ec.contract.repository.*;
import com.ec.contract.util.CurrencyUtil;
import com.itextpdf.io.font.constants.StandardFonts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import com.ec.contract.util.CurrencyUtil;
import com.ec.contract.util.StringUtil;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.VerticalAlignment;
import com.ec.contract.util.CurrencyUtil;
import com.ec.contract.util.StringUtil;
import com.ec.library.exception.CustomException;
import com.ec.library.exception.ResponseCode;
import com.ec.library.response.Response;
import com.itextpdf.io.font.FontProgramFactory;
import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.element.Paragraph;



import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChangeFileService {

    private final ContractRepository contractRepository;
    private final FieldRepository fieldRepository;
    private final DocumentRepository documentRepository;
    private final DocumentService documentService;
    private final FileService fileService;

    @Value("${minio.bucket-name}")
    private String bucketName;

    @Value("${application.temporary.directory}")
    private String tempFolder;

    public void byPassContractNo(ContractResponseDTO contractDto) {
        log.info("Bắt đầu thêm contractNo vào Hợp đồng");
        //log.info("add text: " + contractId);
        String tempFilePath = null;
        int contractId = contractDto.getId();
        String pathFileBackUp = null;
        String bucketMinio = null;
        String pathMinio = null;
        try {
            log.info("contractId: {}", contractId);
            final var documentCollection = documentRepository.findAllByContractIdAndStatusOrderByIdDesc(
                    contractId, BaseStatus.ACTIVE.ordinal()
            );

            final var docOptional = documentCollection.stream()
                    .filter(document -> document.getType() == DocumentType.PRIMARY.getDbVal())
                    .findFirst();

            if (docOptional.isPresent()) {
                final var doc = docOptional.get();

                // lấy thông tin trường dữ liệu cần thêm
                final var fieldCollection = fieldRepository.findByContractIdOrderByTypeAsc(contractId);

                final var fieldList = fieldCollection.stream()
                        .filter(field ->  (field.getType() == FieldType.CONTRACT_NO.getDbVal()
                                        || field.getType() == FieldType.TEXT.getDbVal()
                                        || field.getType() == FieldType.CURRENCY.getDbVal()
                                )
                        )
                        .collect(Collectors.toList());

                Map<String,String> mapPresignedUrl = documentService.getPresignedUrl(doc.getId());

                final var urlFileBefore = mapPresignedUrl.get("message");

                try {
                    log.info("Bắt đầu tạo ra file backup trước khi add ô text");

                    byte[] fileBeforeAction = new URL(urlFileBefore).openStream().readAllBytes();
                    String fileUrl = String.format("%s/%s_bak.pdf", tempFolder, UUID.randomUUID());
                    try (FileOutputStream fileOutputStream = new FileOutputStream(fileUrl)) {
                        fileOutputStream.write(fileBeforeAction);
                    }

                    // đảm bảo phải ghi file thành công mới đến bước set đường dẫn
                    pathFileBackUp = fileUrl;
                    bucketMinio = bucketName;
                    pathMinio = doc.getPath();
                    log.info("Tạo file backup thành công");
                }catch (Exception ignored){}

                for(Field field : fieldList) {

                    final var presignedUrl = urlFileBefore;

                    String text = field.getValue();

                    if (field.getType() == FieldType.CONTRACT_NO.getDbVal() && field.getRecipientId() == null) {
                        text = contractDto.getContractNo();
                    }

                    try {
                        if (field.getType() == FieldType.CURRENCY.getDbVal()) {
                            text = CurrencyUtil.format(Long.valueOf(text));
                        }
                    } catch (NumberFormatException e) {

                    }

                    tempFilePath = addText(
                            presignedUrl,
                            field.getPage(),
                            field.getBoxX().floatValue(),
                            field.getBoxY().floatValue(),
                            field.getBoxH().floatValue(),
                            field.getBoxW().floatValue(),
                            "Times New Roman",
                            11,
                            text,
                            false
                    );

                    log.info("tmp file: " + tempFilePath);
                    if (!(StringUtils.hasText(tempFilePath))){
                        throw new RuntimeException("Đã có lỗi xảy ra trong quá trình add text !");
                    }

                    // thay thế nội dung tệp tin trên hệ thống MinIO
                    var res = fileService.replace(tempFilePath, doc.getId());

                    log.info("replace file: " + !res.isEmpty());
                    // log.info("==> add text contract_id="+field.getContractId()+" thành công");
                }
            } else {
                log.error("by pass add text error: can't find document where contract_id = {}", contractId);
            }
        } catch (Exception e) {
            log.error("Lỗi tại hàm byPassContractNo bắt đầu backup File ban đầu");
            fileService.backUpFileMinioIfErrorAction(pathFileBackUp, contractId);
            log.error("can't by pass add text iiuajhsdg: ", e);
            throw new RuntimeException(e.getMessage());
        } finally {
            if (StringUtils.hasText(tempFilePath)) {
                try {
                    FileUtils.deleteDirectory(new File(tempFilePath));
                } catch (Exception e) {
                    log.error("can't delete directory {}", tempFilePath);
                }
            }
        }
    }

    /**
     * Thêm trường nội dung dạng chuỗi ký tự vào nội dung tệp tin pdf
     *
     * @param filePath  Đường dẫn tới tệp tin pdf
     * @param pageIndex Trang cần thêm nội dung
     * @param tx        Toạ độ điểm cần thêm nội dung, theo trục dọc
     * @param ty        Toạ đô điểm cần thêm nội dung, theo trục ngang
     * @param height    Chiều cao ô text
     * @param width     Chiều dài ô text
     * @param fontSize  Kích thước của định dạng
     * @param text      Nội dung cần thêm vào tệp tin pdf
     * @param isColor   Tô màu text hay không?
     * @return Đường dẫn tạm của tệp tin sau khi xử lý
     */
    private String addText(String filePath, int pageIndex, float tx, float ty, float height, float width, String fontName, int fontSize, String text, boolean isColor) {
        try {
            var pdfReader = new PdfReader(new URL(filePath).openStream());
            String newFilePath = String.format("%s/%s.pdf", tempFolder, UUID.randomUUID());
            var pdfWriter = new PdfWriter(newFilePath);
            var pdfDoc = new PdfDocument(pdfReader, pdfWriter);
            int rotation = pdfDoc.getPage(pageIndex).getRotation();

            if (rotation == 0) {
                // convert top left -> bottom left
                // FE cong chieu cao tat ca cac trang vao toa do y (y_n = page(0).h + page(1).h + .... + page(n).h)
                for (int i = 1; i < pageIndex; i++) {
                    ty = ty - (float) Math.floor(pdfDoc.getPage(i).getPageSize().getHeight());
                }

                ty = (float) (Math.floor(pdfDoc.getPage(pageIndex).getPageSize().getHeight()) - ty - height + (pageIndex - 1) * 5.0);
            } else {
                if (text.startsWith("Mã HD:")) {
                    tx = 10.0f;
                    ty = 15.0f;
                } else {
                    // FE cong chieu cao tat ca cac trang vao toa do y (y_n = page(0).h + page(1).h + .... + page(n).h)
                    for (int i = 1; i < pageIndex; i++) {
                        ty = ty - (float) Math.floor(pdfDoc.getPage(i).getPageSize().getWidth());
                    }
                    ty = (float) (ty - (pageIndex - 1) * 5.0);
                    float temp = tx;
                    tx = ty;
                    ty = temp;
                }
            }

            fontName = (StringUtils.hasText(fontName) ? fontName : "Times New Roman");
//            var fontProgram = FontProgramFactory.createFont(String.format("./resources/fonts/%s.ttf", fontName));
//            var font = PdfFontFactory.createFont(fontProgram, PdfEncodings.IDENTITY_H);

            var font = PdfFontFactory.createFont(StandardFonts.HELVETICA);


            final var textList = StringUtil.autoSplit(text, width, font, fontSize);

            Document document = new Document(pdfDoc);

            var paragraph = new Paragraph(textList.get(0)).setFont(font).setFontSize(fontSize);
            if (isColor) {
                paragraph.setFontColor(ColorConstants.BLUE);
            }

            log.info("add text at: {}, {}, {}", pageIndex, tx, ty);
            document.showTextAligned(paragraph, tx + 1, ty - 2, pageIndex,
                    TextAlignment.LEFT, VerticalAlignment.BOTTOM, (float) Math.toRadians(rotation));

            if (textList.size() > 1) {
                var paragraphLine2 = new Paragraph(textList.get(1)).setFont(font).setFontSize(fontSize);
                if (isColor) {
                    paragraphLine2.setFontColor(ColorConstants.BLUE);
                }
                document.showTextAligned(paragraphLine2, tx + 1, ty - 2, pageIndex, TextAlignment.LEFT, VerticalAlignment.TOP, (float) Math.toRadians(rotation));
            }

            document.close();

            return newFilePath;
        } catch (IOException e) {
            log.error(String.format("can't load pdf at \"%s\"", filePath), e);
        } catch (Exception e) {
            log.error(String.format("can't write text \"%s\" to pdf at \"%s\"", text, filePath), e);
        }

        return null;
    }

    @Transactional
    public void byPassContractUid(int contractId) {
        log.info("Bắt đầu thêm contractUid vào pdf hợp đồng");
        String tempFilePath = null;
        String pathFileBackUp = null;
        String pathMinio = null;
        String bucketMinio = null;

        try {
            // lấy thông tin hợp đồng
            final var contractOptional = contractRepository.findById(contractId);

            if(contractOptional.isPresent()){
                final var contract = contractOptional.get();

                final var documentCollection = documentRepository.findAllByContractIdAndStatusOrderByIdDesc(
                        contractId, BaseStatus.ACTIVE.ordinal()
                );

                final var docOptional = documentCollection.stream()
                        .filter(document -> document.getType() == DocumentType.PRIMARY.getDbVal())
                        .findFirst();

                if (docOptional.isPresent()) {
                    final var doc = docOptional.get();

                    Map<String,String> mapPresignedUrl = documentService.getPresignedUrl(doc.getId());

                    final var presignedUrl = mapPresignedUrl.get("message");

                    try {
                        log.info("Bắt đầu tạo ra file backup trước khi add ô text");
                        final var urlFileBefore = presignedUrl;
                        byte[] fileBeforeAction = new URL(urlFileBefore).openStream().readAllBytes();
                        String fileUrl = String.format("%s/%s_bak.pdf", tempFolder, UUID.randomUUID());
                        try (FileOutputStream fileOutputStream = new FileOutputStream(fileUrl)) {
                            fileOutputStream.write(fileBeforeAction);
                        }

                        // đảm bảo phải ghi file thành công mới đến bước set đường dẫn
                        pathFileBackUp = fileUrl;
                        bucketMinio = bucketName;
                        pathMinio = doc.getPath();
                        log.info("Tạo file backup thành công");
                    }catch (Exception ignored){}

                    tempFilePath = addText(
                            presignedUrl,
                            1,
                            15,
                            0,
                            25,
                            150,
                            "Times New Roman",
                            11,
                            "Mã HD: " + contract.getContractNo(),
                            true
                    );

                    log.info("tmp file: " + tempFilePath);

                    if (!StringUtils.hasText(tempFilePath)) {
                        throw new RuntimeException("Add contractUid thất bại");
                    }

                    // thay thế nội dung tệp tin trên hệ thống MinIO
                    var res = fileService.replace(tempFilePath, doc.getId());

                    log.info("replace file: " + !res.isEmpty());
                } else {
                    log.error("by pass add text error: can't find document where contract_id = {}", contractId);
                }
            } else {
                log.error("Can't find contract_id = {}", contractId);
            }
        } catch (Exception e) {
            log.error("Lỗi tại hàm byPassContractUid bắt đầu backup File ban đầu");
            fileService.backUpFileMinioIfErrorAction(pathFileBackUp, contractId);
            log.error("can't by pass add text thth: ", e);
            throw new RuntimeException(e.getMessage());
        } finally {
            if (StringUtils.hasText(tempFilePath)) {
                try {
                    FileUtils.deleteDirectory(new File(tempFilePath));
                } catch (Exception e) {
                    log.error("can't delete directory {}", tempFilePath);
                }
            }
        }
    }
}
