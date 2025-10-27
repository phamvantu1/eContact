package com.ec.contract.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.Delegate;
import org.hibernate.validator.constraints.Length;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

/**
 * Đối tượng lưu trữ thông tin các trường dữ liệu cần điền,
 * trong quá trình ký hợp đồng
 *
 * @author VHC JSC
 * @version 1.0
 * @since 1.0
 */
@Data
@NoArgsConstructor
@ToString
public class FieldDto implements Serializable {

    private Integer id;

    private String name;

    private int type;

    private String value;

    @NotBlank(message = "Font is mandatory")
    @Length(max = 63, message = "Font '${validatedValue}' must be less than {max} characters long")
    private String font;

    @Min(value = 1, message = "Font size '${validatedValue}' must be greater than {value}")
    private short fontSize;

    @Min(value = 1, message = "Page number '${validatedValue}' must be greater than {value}")
    private short page;

    private Double boxX;

    private Double boxY;

    private Double boxW;

    private Double boxH;

    private int status;

    private int contractId;

    private int documentId;

    private Integer recipientId;

    private RecipientResponse recipient;

    private Integer typeImageSignature;

    private boolean actionInContract;

    private int ordering;

    @Getter
    @Setter
    @ToString
    public static class RecipientResponse implements Serializable {
        private Integer id;
        private String name;
        private String email;
        private String phone;
        private int role;
        private String username;
        private int ordering;
        private int status;

        @JsonFormat(pattern = "yyyy/MM/dd hh:mm:ss")
        private Date fromAt;

        @JsonFormat(pattern = "yyyy/MM/dd hh:mm:ss")
        private Date dueAt;

        @JsonFormat(pattern = "yyyy/MM/dd hh:mm:ss")
        private Date signAt;

        @JsonFormat(pattern = "yyyy/MM/dd hh:mm:ss")
        private Date processAt;

        private Integer signType;

        private String notifyType;

        private Integer remind;

        @JsonFormat(pattern = "yyyy/MM/dd")
        private Date remindDate;

        private String remindMessage;

        private String reasonReject;

        private String cardId;

        @Data
        @NoArgsConstructor
        @ToString
        public static class SignType implements Serializable {
            private int id;
            private String name;
        }
    }
}
