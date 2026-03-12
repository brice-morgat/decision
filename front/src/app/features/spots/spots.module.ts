import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';
import { SharedModule } from '../../shared/shared.module';
import { SpotCatalogComponent } from './pages/spot-catalog.component';

@NgModule({
  declarations: [SpotCatalogComponent],
  imports: [
    SharedModule,
    RouterModule.forChild([{ path: '', component: SpotCatalogComponent }])
  ]
})
export class SpotsModule {}
