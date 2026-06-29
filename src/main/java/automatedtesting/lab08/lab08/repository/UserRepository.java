package automatedtesting.lab08.lab08.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import automatedtesting.lab08.lab08.model.User;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}
