import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { DocumentRoutingModule } from './document-routing.module';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { DocumentsPageComponent } from './pages/documents-page/documents-page.component';
import { DocumentRequestsPageComponent } from './pages/document-requests-page/document-requests-page.component';
import { ReceivedRequestsPageComponent } from './pages/received-requests-page/received-requests-page.component';
import { RequestDocumentDialogComponent } from './components/request-document-dialog/request-document-dialog.component';
import { ProcessRequestDialogComponent } from './components/process-request-dialog/process-request-dialog.component';
import { PublishDocumentDialogComponent } from './components/publish-document-dialog/publish-document-dialog.component';

@NgModule({
  declarations: [
    DocumentsPageComponent,
    DocumentRequestsPageComponent,
    ReceivedRequestsPageComponent,
    RequestDocumentDialogComponent,
    ProcessRequestDialogComponent,
    PublishDocumentDialogComponent,
  ],
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    MatTableModule,
    MatButtonModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    DocumentRoutingModule,
  ],
})
export class DocumentModule {}
