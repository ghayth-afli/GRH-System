package com.otbs.user.service;

import com.otbs.user.dto.UserInfoRequestDTO;
import com.otbs.user.dto.ProfilePictureDTO;
import com.otbs.user.exception.UserException;
import com.otbs.user.exception.FileUploadException;
import com.otbs.user.mapper.UserAttributesMapper;
import com.otbs.user.model.User;
import com.otbs.user.repository.UserInfoRepository;
import com.otbs.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.naming.Name;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);
    private final UserRepository userRepository;
    private final UserInfoRepository userInfoRepository;
    private final UserAttributesMapper userAttributesMapper;

    private User enrichUserRole(User user) {
        if (user != null && "HR".equals(user.getDepartment()) && "Manager".equals(user.getRole())) {
            user.setRole("HRD");
        }
        return user;
    }

    @Override
    public User getUserByDn(Name dn) {
        return userRepository.findById(dn)
                .map(userAttributesMapper)
                .map(this::enrichUserRole)
                .orElseThrow(() -> new UserException("User not found"));
    }

    @Override
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(userAttributesMapper)
                .map(this::enrichUserRole)
                .orElseThrow(() -> new UserException("User not found"));
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userAttributesMapper)
                .map(this::enrichUserRole)
                .filter(user -> !user.getDepartment().equals("Unknown")
                        && !user.getDepartment().equals("Domain Controllers"))
                .toList();
    }

    @Override
    public User getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .map(userAttributesMapper)
                .map(this::enrichUserRole)
                .orElseThrow(() -> new UserException("User not found"));
        log.info("User info retrieved: {}", user);

        Optional<User> userInfoOpt = userInfoRepository.findById(user.getId());

        if (userInfoOpt.isPresent()) {
            User info = userInfoOpt.get();
            user.setFirstName(info.getFirstName());
            user.setLastName(info.getLastName());
            user.setEmail(info.getEmail());
            user.setPicture(info.getPicture());
            user.setPictureType(info.getPictureType());
            user.setJobTitle(info.getJobTitle());
            user.setPhoneNumber1(info.getPhoneNumber1());
            user.setPhoneNumber2(info.getPhoneNumber2());
            user.setGender(info.getGender());
            user.setInfoComplete(info.isInfoComplete());
        } else {
            User userInfo = User.builder()
                    .id(user.getId())
                    .username(user.getUsername())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .email(user.getEmail())
                    .department(user.getDepartment())
                    .role(user.getRole())
                    .isInfoComplete(false)
                    .build();

            User savedUserInfo = userInfoRepository.save(userInfo);
            user.setInfoComplete(savedUserInfo.isInfoComplete());
        }

        log.info("User info retrieved for username: {} - isInfoComplete: {}", user.getUsername(), user.isInfoComplete());
        return user;
    }

    @Override
    public User getManagerByDepartment(String department) {
        return userRepository.findAll().stream()
                .map(userAttributesMapper)
                .filter(user -> user.getDepartment().equals(department))
                .filter(user -> user.getRole().equals("Manager") || user.getRole().equals("HRD"))
                .findFirst()
                .map(this::enrichUserRole)
                .orElseThrow(() -> new UserException("Manager not found"));
    }

    @Override
    public void updateUserInfo(UserInfoRequestDTO userInfoRequestDTO, MultipartFile picture) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        log.info("Updating user info for: {} - Before update isInfoComplete: {}", user.getUsername(), user.isInfoComplete());

        if (picture != null && !picture.isEmpty()) {
            try {
                user.setPicture(picture.getBytes());
                user.setPictureType(picture.getContentType());
            } catch (IOException e) {
                throw new FileUploadException("Failed to upload attachment");
            }
        }

        user.setJobTitle(userInfoRequestDTO.jobTitle());
        user.setEmail(Optional.ofNullable(userInfoRequestDTO.email())
                .filter(email -> !email.isEmpty())
                .orElse(user.getEmail()));
        user.setGender(Optional.ofNullable(userInfoRequestDTO.gender())
                .filter(gender -> !gender.isEmpty())
                .orElse(user.getGender()));
        user.setLastName(userInfoRequestDTO.lastName());
        user.setFirstName(userInfoRequestDTO.firstName());
        user.setPhoneNumber1(userInfoRequestDTO.phoneNumber1());
        user.setPhoneNumber2(Optional.ofNullable(userInfoRequestDTO.phoneNumber2())
                .filter(phone -> !phone.isEmpty())
                .orElse(user.getPhoneNumber2()));

        boolean isComplete = isNotEmpty(user.getFirstName()) &&
                isNotEmpty(user.getLastName()) &&
                isNotEmpty(user.getEmail()) &&
                isNotEmpty(user.getJobTitle()) &&
                isNotEmpty(user.getPhoneNumber1()) &&
                isNotEmpty(user.getGender());

        user.setInfoComplete(isComplete);

        log.info("Field values - firstName: {}, lastName: {}, email: {}, jobTitle: {}, phoneNumber1: {}, gender: {}",
                user.getFirstName(), user.getLastName(), user.getEmail(),
                user.getJobTitle(), user.getPhoneNumber1(), user.getGender());
        log.info("Completeness check - firstName: {}, lastName: {}, email: {}, jobTitle: {}, phoneNumber1: {}, gender: {}",
                isNotEmpty(user.getFirstName()), isNotEmpty(user.getLastName()), isNotEmpty(user.getEmail()),
                isNotEmpty(user.getJobTitle()), isNotEmpty(user.getPhoneNumber1()), isNotEmpty(user.getGender()));
        log.info("Updating user info for user: {} - Info complete: {}", user.getUsername(), isComplete);

        User savedUser = userInfoRepository.save(user);
        log.info("User saved successfully - isInfoComplete in DB: {}", savedUser.isInfoComplete());
    }

    private boolean isNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }

    @Override
    public ProfilePictureDTO getProfilePicture(String username) {
        Optional<User> user = userInfoRepository.findByUsername(username);
        return user.filter(value -> value.getPicture() != null).map(value -> new ProfilePictureDTO(value.getPictureType(), value.getPicture())).orElse(null);
    }
}