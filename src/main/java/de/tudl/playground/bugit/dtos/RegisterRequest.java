package de.tudl.playground.bugit.dtos;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Data
public final class RegisterRequest {
    private String requestId;
    private String username;
    private String email;
    private String password;
}
