package de.tudl.playground.bugit.dtos.requests.user;

public record UpdateUserRequest(
        String username,
        String email
) {
}
