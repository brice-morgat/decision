import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';
import { SharedModule } from '../../shared/shared.module';
import { ProfileFormComponent } from './components/profile-form.component';
import { ProfileDetailComponent } from './pages/profile-detail.component';
import { ProfilesListComponent } from './pages/profiles-list.component';

@NgModule({
  declarations: [ProfilesListComponent, ProfileDetailComponent, ProfileFormComponent],
  imports: [
    SharedModule,
    RouterModule.forChild([
      { path: '', component: ProfilesListComponent },
      { path: 'new', component: ProfileDetailComponent },
      { path: ':id', component: ProfileDetailComponent }
    ])
  ]
})
export class ProfilesModule {}
