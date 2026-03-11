import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';
import { SharedModule } from '../shared/shared.module';
import { AppShellComponent } from './components/app-shell.component';

@NgModule({
  declarations: [AppShellComponent],
  imports: [SharedModule, RouterModule],
  exports: [AppShellComponent]
})
export class LayoutModule {}