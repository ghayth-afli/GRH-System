package com.otbs.training.mapper;

import com.otbs.feign.client.user.UserClient;
import com.otbs.feign.client.user.dto.UserResponse;
import com.otbs.training.dto.InvitationResponseDTO;
import com.otbs.training.model.Invitation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InvitationMapper {

    private final UserClient userClient;

    public InvitationResponseDTO toResponseDTO(Invitation invitation) {
        UserResponse user = userClient.getUserByDn(invitation.getUserId());
        return new InvitationResponseDTO(
                invitation.getId(),
                user.firstName() + " " + user.lastName(),
                invitation.getStatus(),
                user.id()
        );
    }


}
