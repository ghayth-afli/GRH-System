import { Component, inject } from '@angular/core';
import { InvitationService } from '../../services/invitation.service';

@Component({
  selector: 'app-invitations-page',
  standalone: false,

  templateUrl: './invitations-page.component.html',
  styleUrl: './invitations-page.component.css',
})
export class InvitationsPageComponent {
  private invitationService = inject(InvitationService);
}
