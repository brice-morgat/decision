import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';
import { SharedModule } from '../../shared/shared.module';
import { RuleActionListComponent } from './components/rule-action-list.component';
import { RuleConditionListComponent } from './components/rule-condition-list.component';
import { RuleFiltersComponent } from './components/rule-filters.component';
import { RuleDetailComponent } from './pages/rule-detail.component';
import { RulesCatalogComponent } from './pages/rules-catalog.component';

@NgModule({
  declarations: [
    RulesCatalogComponent,
    RuleDetailComponent,
    RuleFiltersComponent,
    RuleConditionListComponent,
    RuleActionListComponent
  ],
  imports: [
    SharedModule,
    RouterModule.forChild([
      { path: '', component: RulesCatalogComponent },
      { path: 'new', component: RuleDetailComponent },
      { path: ':id', component: RuleDetailComponent }
    ])
  ]
})
export class RulesModule {}
