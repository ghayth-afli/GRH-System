package com.otbs.employee.service;

import com.otbs.employee.dto.EmployeeInfoRequestDTO;
import com.otbs.employee.dto.ProfilePictureDTO;
import com.otbs.employee.model.Employee;
import org.springframework.web.multipart.MultipartFile;

import javax.naming.Name;
import java.util.List;

public interface EmployeeService {
    Employee getEmployeeByDn(Name dn);
    Employee getEmployeeByEmail(String email);
    List<Employee> getAllEmployees();
    Employee getEmployeeByUsername(String username);
    Employee getManagerByDepartment(String department);
    void updateEmployeeInfo(EmployeeInfoRequestDTO employeeInfoRequestDTO, MultipartFile picture);
    ProfilePictureDTO getProfilePicture(String id);
}
