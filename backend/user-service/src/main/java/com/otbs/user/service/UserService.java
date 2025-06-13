package com.otbs.user.service;

import com.otbs.user.dto.UserInfoRequestDTO;
import com.otbs.user.dto.ProfilePictureDTO;
import com.otbs.user.model.User;
import org.springframework.web.multipart.MultipartFile;

import javax.naming.Name;
import java.util.List;

public interface UserService {
    User getUserByDn(Name dn);
    User getUserByEmail(String email);
    List<User> getAllUsers();
    User getUserByUsername(String username);
    User getManagerByDepartment(String department);
    void updateUserInfo(UserInfoRequestDTO userInfoRequestDTO, MultipartFile picture);
    ProfilePictureDTO getProfilePicture(String id);
}
