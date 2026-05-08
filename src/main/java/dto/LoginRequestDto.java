package dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class LoginRequestDto {
    @NotBlank
    private String email;
    @NotBlank
    private String password;
}
