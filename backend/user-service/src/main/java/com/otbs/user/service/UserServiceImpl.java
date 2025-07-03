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
                .filter(user -> !List.of("Unknown", "Domain Controllers").contains(user.getDepartment()))
                .toList();
    }

    @Override
    public User getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .map(userAttributesMapper)
                .map(this::enrichUserRole)
                .orElseThrow(() -> new UserException("User not found"));

        userInfoRepository.findById(user.getId()).ifPresentOrElse(info -> {
            user.setFirstName(info.getFirstName());
            user.setLastName(info.getLastName());
            user.setEmail(info.getEmail());
            user.setPicture(info.getPicture());
            user.setPictureType(info.getPictureType());
            user.setJobTitle(info.getJobTitle());
            user.setPhoneNumber1(info.getPhoneNumber1());
            user.setPhoneNumber2(info.getPhoneNumber2());
            user.setGender(info.getGender());
            user.setBirthDate(info.getBirthDate());
        }, () -> userInfoRepository.save(User.builder()
                .id(user.getId())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .department(user.getDepartment())
                .role(user.getRole())
                .build()));

        return user;
    }

    @Override
    public User getManagerByDepartment(String department) {
        return userRepository.findAll().stream()
                .map(userAttributesMapper)
                .filter(user -> department.equals(user.getDepartment()))
                .filter(user -> List.of("Manager", "HRD").contains(user.getRole()))
                .findFirst()
                .map(this::enrichUserRole)
                .orElseThrow(() -> new UserException("Manager not found"));
    }

    @Override
    public void updateUserInfo(UserInfoRequestDTO userInfoRequestDTO, MultipartFile picture) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (picture != null && !picture.isEmpty()) {
            try {
                user.setPicture(picture.getBytes());
                user.setPictureType(picture.getContentType());
            } catch (IOException e) {
                throw new FileUploadException("Failed to upload attachment");
            }
        }

        user.setJobTitle(userInfoRequestDTO.jobTitle());
        user.setEmail(Optional.ofNullable(userInfoRequestDTO.email()).filter(email -> !email.isEmpty()).orElse(user.getEmail()));
        user.setGender(Optional.ofNullable(userInfoRequestDTO.gender()).filter(gender -> !gender.isEmpty()).orElse(user.getGender()));
        user.setLastName(userInfoRequestDTO.lastName());
        user.setFirstName(userInfoRequestDTO.firstName());
        user.setPhoneNumber1(userInfoRequestDTO.phoneNumber1());
        user.setPhoneNumber2(Optional.ofNullable(userInfoRequestDTO.phoneNumber2()).filter(phone -> !phone.isEmpty()).orElse(user.getPhoneNumber2()));
        user.setBirthDate(userInfoRequestDTO.birthdate());

        userInfoRepository.save(user);
    }

    @Override
    public ProfilePictureDTO getProfilePicture(String username) {
        return userInfoRepository.findByUsername(username)
                .filter(user -> user.getPicture() != null)
                .map(user -> new ProfilePictureDTO(user.getPictureType(), user.getPicture()))
                .orElse(null);
    }
}