package com.otbs.employee.service;

import com.otbs.employee.dto.EmployeeInfoRequestDTO;
import com.otbs.employee.dto.ProfilePictureDTO;
import com.otbs.employee.exception.EmployeeException;
import com.otbs.employee.exception.FileUploadException;
import com.otbs.employee.mapper.UserAttributesMapper;
import com.otbs.employee.model.Employee;
import com.otbs.employee.repository.EmployeeInfoRepository;
import com.otbs.employee.repository.EmployeeRepository;
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
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final EmployeeInfoRepository employeeInfoRepository;
    private final UserAttributesMapper userAttributesMapper;

    @Override
    public Employee getEmployeeByDn(Name dn) {
        return employeeRepository.findById(dn)
                .map(userAttributesMapper)
                .orElseThrow(() -> new EmployeeException("Employee not found"));
    }

    @Override
    public Employee getEmployeeByEmail(String email) {
        return employeeRepository.findByEmail(email)
                .map(userAttributesMapper)
                .orElseThrow(() -> new EmployeeException("Employee not found"));
    }

    @Override
    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll().stream()
                .map(userAttributesMapper)
                .filter(employee -> !employee.getDepartment().equals("Unknown")
                        && !employee.getDepartment().equals("Domain Controllers"))
                .toList();
    }

    @Override
    public Employee getEmployeeByUsername(String username) {
        Employee employee = employeeRepository.findByUsername(username)
                .map(userAttributesMapper)
                .orElseThrow(() -> new EmployeeException("Employee not found"));

        employeeInfoRepository.findById(employee.getId())
                .ifPresentOrElse(
                        info -> {
                        employee.setFirstName(info.getFirstName());
                        employee.setLastName(info.getLastName());
                        employee.setEmail(info.getEmail());
                        employee.setPicture(info.getPicture());
                        employee.setPictureType(info.getPictureType());
                        employee.setJobTitle(info.getJobTitle());
                        employee.setPhoneNumber1(info.getPhoneNumber1());
                        employee.setPhoneNumber2(info.getPhoneNumber2());
                        },
                        () -> {
                            Employee employeeInfo = Employee.builder()
                                    .id(employee.getId())
                                    .username(employee.getUsername())
                                    .firstName(employee.getFirstName())
                                    .lastName(employee.getLastName())
                                    .email(employee.getEmail())
                                    .department(employee.getDepartment())
                                    .role(employee.getRole())
                                    .build();
                            employeeInfoRepository.save(employeeInfo);
                        }
                );

        return employee;
    }

    @Override
    public Employee getManagerByDepartment(String department) {
        return employeeRepository.findAll().stream()
                .map(userAttributesMapper)
                .filter(employee -> employee.getDepartment().equals(department))
                .filter(employee -> employee.getRole().equals("Manager"))
                .findFirst()
                .orElseThrow(() -> new EmployeeException("Manager not found"));

    }

    @Override
    public void updateEmployeeInfo(EmployeeInfoRequestDTO employeeInfoRequestDTO, MultipartFile picture) {

        Employee employee = (Employee) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (picture != null && !picture.isEmpty()) {
            try {
                employee.setPicture(picture.getBytes());
                employee.setPictureType(picture.getContentType());
            } catch (IOException e) {
                throw new FileUploadException("Failed to upload attachment");
            }
        }

        employee.setJobTitle(employeeInfoRequestDTO.jobTitle());
        employee.setEmail(employeeInfoRequestDTO.email());
        employee.setLastName(employeeInfoRequestDTO.lastName());
        employee.setFirstName(employeeInfoRequestDTO.firstName());
        employee.setPhoneNumber1(employeeInfoRequestDTO.phoneNumber1());
        employee.setPhoneNumber2(employeeInfoRequestDTO.phoneNumber2());
        employeeInfoRepository.save(employee);
    }

    @Override
    public ProfilePictureDTO getProfilePicture(String username) {
        Optional<Employee> employee = employeeInfoRepository.findByUsername(username);
        return employee.filter(value -> value.getPicture() != null).map(value -> new ProfilePictureDTO(value.getPictureType(), value.getPicture())).orElse(null);
    }

}