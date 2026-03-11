import { ChangeDetectionStrategy, Component } from '@angular/core';
import { FormBuilder } from '@angular/forms';
import { Router } from '@angular/router';
import { BehaviorSubject, combineLatest, map, shareReplay, switchMap } from 'rxjs';
import { DecisionRuleFilters, DecisionRuleSummary } from '../../../core/models/rule.models';
import { ProfilesService } from '../../../core/services/profiles.service';
import { RulesService } from '../../../core/services/rules.service';

@Component({
  selector: 'app-rules-catalog',
  templateUrl: './rules-catalog.component.html',
  styleUrls: ['./rules-catalog.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class RulesCatalogComponent {
  private readonly filtersRefresh$ = new BehaviorSubject<DecisionRuleFilters>({});

  readonly filtersForm = this.fb.group({
    profileId: [null as string | null],
    gameType: [null],
    street: [null],
    scenarioType: [null],
    enabled: [null as boolean | null],
    heroPosition: [null],
    villainPosition: [null],
    priority: [null as number | null]
  });

  readonly profiles$ = this.profilesService.list().pipe(shareReplay({ bufferSize: 1, refCount: true }));
  readonly rules$ = this.filtersRefresh$.pipe(
    switchMap((filters) => this.rulesService.list(filters)),
    shareReplay({ bufferSize: 1, refCount: true })
  );
  readonly viewModel$ = combineLatest([this.profiles$, this.rules$]).pipe(
    map(([profiles, rules]) => ({ profiles, rules }))
  );

  constructor(
    private readonly fb: FormBuilder,
    private readonly router: Router,
    private readonly profilesService: ProfilesService,
    private readonly rulesService: RulesService
  ) {}

  applyFilters(): void {
    this.filtersRefresh$.next(this.filtersForm.getRawValue());
  }

  resetFilters(): void {
    this.filtersForm.reset({
      profileId: null,
      gameType: null,
      street: null,
      scenarioType: null,
      enabled: null,
      heroPosition: null,
      villainPosition: null,
      priority: null
    });
    this.applyFilters();
  }

  openRule(id: string): void {
    void this.router.navigate(['/rules', id]);
  }

  createRule(): void {
    void this.router.navigate(['/rules', 'new']);
  }

  duplicateRule(rule: DecisionRuleSummary): void {
    const name = window.prompt('Nom de la copie', `${rule.name} (copy)`);
    if (name === null) {
      return;
    }

    this.rulesService.duplicate(rule.id, { name: name || null }).subscribe((created) => {
      this.applyFilters();
      void this.router.navigate(['/rules', created.id]);
    });
  }

  deleteRule(rule: DecisionRuleSummary): void {
    if (!window.confirm(`Supprimer la regle "${rule.name}" ?`)) {
      return;
    }

    this.rulesService.delete(rule.id).subscribe(() => {
      this.applyFilters();
    });
  }

  trackById(_: number, rule: DecisionRuleSummary): string {
    return rule.id;
  }
}
