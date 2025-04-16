package com.otbs.training.service;

public interface InvitationService {
    void confirmInvitation(Long invitationId);
    void rejectInvitation(Long invitationId);
}
