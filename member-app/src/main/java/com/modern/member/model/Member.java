package com.modern.member.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@EqualsAndHashCode(callSuper = true)
@Document(collection = "members")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Member extends BaseEntity {

    @Id
    private String id;

    @NotBlank(message = "Name cannot be empty")
    @Size(min = 1, max = 25, message = "Name size must be between 1 and 25")
    @Pattern(regexp = "[^0-9]*", message = "Name must not contain numbers")
    private String name;

    @NotBlank(message = "Email cannot be empty")
    @Email(message = "Must be a well-formed email address")
    @Indexed(unique = true)
    private String email;

    @NotBlank(message = "Phone number cannot be empty")
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Phone number must be valid 10-digit Indian number")
    @Field("phone_number")
    private String phoneNumber;

    @NotBlank(message = "Username is required")
    @Indexed(unique = true)
    private String userName;

    @NotBlank(message = "Password is required")
    @JsonIgnore
    private String password;

    private Role role;

    @Builder.Default
    private int tokenVersion = 1;

    @Builder.Default
    private boolean isPasswordTemporary = true;
}