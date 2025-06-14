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

    /**
     * Applies business logic to a User object.
     * If the user is in the "HR" department and has the "Manager" role, it changes the role to "HRD".
     * @param user The user object to process.
     * @return The user object with potentially an updated role.
     */
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
                .map(this::enrichUserRole) // Apply role enrichment
                .orElseThrow(() -> new UserException("User not found"));
    }



    @Override
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(userAttributesMapper)
                .map(this::enrichUserRole) // Apply role enrichment
                .orElseThrow(() -> new UserException("User not found"));
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userAttributesMapper)
                .map(this::enrichUserRole) // Apply role enrichment
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

        userInfoRepository.findById(user.getId())
                .ifPresentOrElse(
                        info -> {
                            user.setFirstName(info.getFirstName());
                            user.setLastName(info.getLastName());
                            user.setEmail(info.getEmail());
                            user.setPicture(info.getPicture());
                            user.setPictureType(info.getPictureType());
                            user.setJobTitle(info.getJobTitle());
                            user.setPhoneNumber1(info.getPhoneNumber1());
                            user.setPhoneNumber2(info.getPhoneNumber2());
                        },
                        () -> {
                            User userInfo = User.builder()
                                    .id(user.getId())
                                    .username(user.getUsername())
                                    .firstName(user.getFirstName())
                                    .lastName(user.getLastName())
                                    .email(user.getEmail())
                                    .department(user.getDepartment())
                                    // The role will already be enriched from the 'user' object
                                    .role(user.getRole())
                                    .build();
                            userInfoRepository.save(userInfo);
                        }
                );

        return user;
    }

    @Override
    public User getManagerByDepartment(String department) {
        // Here, we find the manager based on the original "Manager" role first,
        // and then apply the enrichment to the final result.
        return userRepository.findAll().stream()
                .map(userAttributesMapper)
                .filter(user -> user.getDepartment().equals(department))
                .filter(user -> user.getRole().equals("Manager") || user.getRole().equals("HRD"))
                .findFirst()
                .map(this::enrichUserRole) // Apply role enrichment to the found manager
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
        user.setEmail(userInfoRequestDTO.email());
        user.setLastName(userInfoRequestDTO.lastName());
        user.setFirstName(userInfoRequestDTO.firstName());
        user.setPhoneNumber1(userInfoRequestDTO.phoneNumber1());
        user.setPhoneNumber2(userInfoRequestDTO.phoneNumber2());
        userInfoRepository.save(user);
    }

    @Override
    public ProfilePictureDTO getProfilePicture(String username) {
        Optional<User> user = userInfoRepository.findByUsername(username);
        return user.filter(value -> value.getPicture() != null).map(value -> new ProfilePictureDTO(value.getPictureType(), value.getPicture())).orElse(null);
    }

}
