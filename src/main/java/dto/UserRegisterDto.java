package dto;


import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class UserRegisterDto {
    @NotBlank
    private String name;

    @NotBlank
    private String email;

    @NotBlank
    private long phone;

    @NotBlank
    private String password;
}
