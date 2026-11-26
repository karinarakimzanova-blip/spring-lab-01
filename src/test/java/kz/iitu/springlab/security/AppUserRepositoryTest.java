package kz.iitu.springlab.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class AppUserRepositoryTest {

    @Autowired
    AppUserRepository appUserRepository;

    @Test
    void savesAndFindsUserByUsername() {
        AppUser saved = appUserRepository.save(
                new AppUser("alice", "$2a$10$encodedPassword", true, Set.of("USER"))
        );

        Optional<AppUser> found = appUserRepository.findByUsername("alice");

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(saved.getId());
        assertThat(found.get().getUsername()).isEqualTo("alice");
        assertThat(found.get().getRoles()).contains("USER");
    }

    @Test
    void returnsEmptyWhenUsernameDoesNotExist() {
        Optional<AppUser> found = appUserRepository.findByUsername("nobody");

        assertThat(found).isEmpty();
    }
}
