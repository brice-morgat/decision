import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';
import { SharedModule } from '../../shared/shared.module';
import { DecisionContextFormComponent } from './components/decision-context-form.component';
import { DecisionResultCardComponent } from './components/decision-result-card.component';
import { DecisionAssistantPageComponent } from './pages/decision-assistant-page.component';

@NgModule({
  declarations: [
    DecisionAssistantPageComponent,
    DecisionContextFormComponent,
    DecisionResultCardComponent
  ],
  imports: [
    SharedModule,
    RouterModule.forChild([{ path: '', component: DecisionAssistantPageComponent }])
  ]
})
export class DecisionAssistantModule {}