package com.modern.kitchensink.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@EqualsAndHashCode(callSuper = true)
@Document(collection = "members")
public class Member extends BaseEntity {

    @Id
    private String id;

    @NotBlank(message = "Name cannot be empty")
    @Size(min = 1, max = 25, message = "Name size must be between 1 and 25")
    private String name;

    @NotBlank(message = "Email cannot be empty")
    @Email(message = "Must be a well-formed email address")
    private String email;

    @NotBlank(message = "Phone number cannot be empty")
    @Pattern(regexp = "\\d{10,12}", message = "Phone number must be between 10 and 12 digits")
    private String phoneNumber;
}