package com.otbs.leave.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "leave_balance")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LeaveBalance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String userDn;

    @Column(nullable = false)
    private Double totalLeave;

    @Column(nullable = false)
    private Double usedLeave;

    @Column(nullable = false)
    private Double remainingLeave;

    @Column(nullable = false)
    private LocalDate lastUpdatedDate;

    public LeaveBalance(String userDn, Double totalLeave, Double usedLeave, Double remainingLeave) {
        this.userDn = userDn;
        this.totalLeave = totalLeave;
        this.usedLeave = usedLeave;
        this.remainingLeave = remainingLeave;
        this.lastUpdatedDate = LocalDate.now();
    }


    public void addMonthlyLeave() {
        if (lastUpdatedDate != null && lastUpdatedDate.plusMonths(1).isBefore(LocalDate.now())) {
            totalLeave += 2.5;
            remainingLeave = totalLeave - usedLeave;
            lastUpdatedDate = LocalDate.now();
        }
    }
}