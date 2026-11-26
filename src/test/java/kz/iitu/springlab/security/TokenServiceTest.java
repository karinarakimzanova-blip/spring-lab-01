package kz.iitu.springlab.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TokenServiceTest {

    @Mock
    AppUserRepository userRepository;

    @InjectMocks
    TokenService tokenService;

    @Test
    void issuesTokenAndExposesExpectedDecodedClaimsForKnownScope() {
        AppUser user = new AppUser("librarian", "$2a$10$hash", true, Set.of("USER", "LIBRARIAN"));
        given(userRepository.findByUsername("librarian")).willReturn(Optional.of(user));

        String token = tokenService.issueToken("librarian", "books:write");
        Map<String, Object> claims = tokenService.decodeClaims(token);

        assertThat(token).isNotBlank();
        assertThat(claims)
                .containsEntry("sub", "librarian")
                .containsEntry("scope", "books:write");

        @SuppressWarnings("unchecked")
        List<String> roles = (List<String>) claims.get("roles");
        assertThat(roles).containsExactlyInAnyOrder("USER", "LIBRARIAN");
    }

    @Test
    void rejectsTokenRequestWhenUserDoesNotExist() {
        given(userRepository.findByUsername("missing")).willReturn(Optional.empty());

        assertThatThrownBy(() -> tokenService.issueToken("missing", "books:read"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void queriesRepositoryWithExactUsernameCapturedByArgumentCaptor() {
        AppUser user = new AppUser("admin", "$2a$10$hash", true, Set.of("ADMIN"));
        given(userRepository.findByUsername("admin")).willReturn(Optional.of(user));

        tokenService.issueToken("admin", "admin:read");

        ArgumentCaptor<String> usernameCaptor = ArgumentCaptor.forClass(String.class);
        verify(userRepository).findByUsername(usernameCaptor.capture());
        assertThat(usernameCaptor.getValue()).isEqualTo("admin");
    }

    @Test
    void rejectsTokenRequestWhenScopeIsUnknown() {
        assertThatThrownBy(() -> tokenService.issueToken("admin", "unknown:scope"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unknown scope");
    }
}