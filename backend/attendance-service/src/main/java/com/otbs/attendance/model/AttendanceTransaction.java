package com.otbs.attendance.model;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "iclock_transaction")
public class AttendanceTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "punch_time", nullable = false)
    private Instant punchTime;

    @Column(name = "punch_state", nullable = false)
    private String punchState;

    @Column(name = "verify_type", nullable = false)
    private Integer verifyType;

    @Column(name = "terminal_sn")
    private String terminalSn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emp_id", referencedColumnName = "id")
    private Employee employee;
}
