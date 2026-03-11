import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';
import { SharedModule } from '../../shared/shared.module';
import { HeroRangeCellEditorComponent } from './components/hero-range-cell-editor.component';
import { PokerHandMatrixComponent } from './components/poker-hand-matrix.component';
import { VillainRangeCellEditorComponent } from './components/villain-range-cell-editor.component';
import { HeroRangesComponent } from './pages/hero-ranges.component';
import { VillainRangesComponent } from './pages/villain-ranges.component';

@NgModule({
  declarations: [
    HeroRangesComponent,
    VillainRangesComponent,
    PokerHandMatrixComponent,
    HeroRangeCellEditorComponent,
    VillainRangeCellEditorComponent
  ],
  imports: [
    SharedModule,
    RouterModule.forChild([
      { path: 'hero', component: HeroRangesComponent },
      { path: 'villain', component: VillainRangesComponent },
      { path: '', pathMatch: 'full', redirectTo: 'hero' }
    ])
  ]
})
export class RangesModule {}
