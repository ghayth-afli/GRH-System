import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { DocumentsPageComponent } from './pages/documents-page/documents-page.component';
import { DocumentRequestsPageComponent } from './pages/document-requests-page/document-requests-page.component';
import { ReceivedRequestsPageComponent } from './pages/received-requests-page/received-requests-page.component';

const routes: Routes = [
  { path: 'documents', component: DocumentsPageComponent },
  { path: 'document-requests', component: DocumentRequestsPageComponent },
  { path: 'received-requests', component: ReceivedRequestsPageComponent },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class DocumentRoutingModule {}
