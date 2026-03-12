package chetmine.marketplace.parser.service;

import chetmine.marketplace.parser.dto.CreateUserDTO;
import chetmine.marketplace.parser.entity.User;
import chetmine.marketplace.parser.entity.UserStatus;
import chetmine.marketplace.parser.repo.UserRepo;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {
    private final UserRepo userRepo;

    @Transactional
    public List<User> findAll() {
        return this.userRepo.findAll();
    }

    public User findById(long id) {
        return this.userRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found"))
                ;
    }

    public User findByEmail(String email) {
        return this.userRepo.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

    public User create(CreateUserDTO dto) {
        if (userRepo.existsByEmail(dto.email())) {
            throw new EntityExistsException("Email already registered.");
        }

        User user = new User();
        user.setEmail(dto.email());
        user.setPasswordHash(dto.passwordHash());

        return userRepo.save(user);
    }

    public void verify(Long id) {
        User user = this.findById(id);
        if (user == null) {
            throw new EntityNotFoundException("User not found");
        }

        user.setStatus(UserStatus.VERIFIED);
    }

    public void register(Long id) {
        User user = this.findById(id);
        if (user == null) {
            throw new EntityNotFoundException("User not found");
        }

        user.setSessionId(UUID.randomUUID().toString());
        user.setRegistered(true);
    }

    public boolean existsEmail(String email) {
        return this.userRepo.existsByEmail(email);
    }
}
