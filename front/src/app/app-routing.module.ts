import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

const routes: Routes = [
  {
    path: 'assistant',
    loadChildren: () =>
      import('./features/decision-assistant/decision-assistant.module').then(
        (m) => m.DecisionAssistantModule
      )
  },
  {
    path: 'profiles',
    loadChildren: () =>
      import('./features/profiles/profiles.module').then((m) => m.ProfilesModule)
  },
  {
    path: 'ranges',
    loadChildren: () =>
      import('./features/ranges/ranges.module').then((m) => m.RangesModule)
  },
  {
    path: 'rules',
    loadChildren: () =>
      import('./features/rules/rules.module').then((m) => m.RulesModule)
  },
  {
    path: 'reviews',
    loadChildren: () =>
      import('./features/reviews/reviews.module').then((m) => m.ReviewsModule)
  },
  {
    path: 'settings',
    loadChildren: () =>
      import('./features/settings/settings.module').then((m) => m.SettingsModule)
  },
  { path: '', pathMatch: 'full', redirectTo: 'assistant' },
  { path: '**', redirectTo: 'assistant' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes, { bindToComponentInputs: true })],
  exports: [RouterModule]
})
export class AppRoutingModule {}