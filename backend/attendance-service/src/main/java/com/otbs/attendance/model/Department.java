package com.otbs.attendance.model;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "personnel_department")
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "dept_code", unique = true, nullable = false)
    private String deptCode;

    @Column(name = "dept_name", nullable = false)
    private String deptName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_dept_id")
    private Department parentDepartment;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dept_manager_id")
    private Employee manager;

    @OneToMany(mappedBy = "department")
    private List<Employee> employees;
}
