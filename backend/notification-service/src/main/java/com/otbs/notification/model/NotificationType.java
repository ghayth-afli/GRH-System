package com.otbs.notification.model;


public enum NotificationType {

    // Employee-related
    NEW_EMPLOYEE_ONBOARDING,
    EMPLOYEE_PROFILE_UPDATE,
    EMPLOYEE_TERMINATION,

    // Leave & Attendance
    LEAVE_REQUEST,
    LEAVE_APPROVED,
    LEAVE_REJECTED,
    ATTENDANCE_MISSED,

    // Performance & Appraisals
    PERFORMANCE_REVIEW_REMINDER,
    GOAL_ASSIGNMENT,
    APPRAISAL_RESULT,

    // Payroll & Compensation
    PAYSLIP_AVAILABLE,
    SALARY_REVISION,
    BONUS_ANNOUNCEMENT,

    // Events & Deadlines
    COMPANY_EVENT,
    TRAINING_SESSION,
    POLICY_UPDATE,
    MEDICAL_VISIT,

    // Approvals & Requests
    APPROVAL_REQUIRED,
    REQUEST_APPROVED,
    REQUEST_DENIED,
    DOCUMENT_SUBMISSION_REMINDER,

    // Alerts & Warnings
    CONTRACT_EXPIRY,
    PERFORMANCE_WARNING
}

