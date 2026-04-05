import { ChangeDetectionStrategy, ChangeDetectorRef, Component, DestroyRef, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { UntypedFormArray, UntypedFormBuilder, UntypedFormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { Observable, of, switchMap } from 'rxjs';
import { StrategicProfileSummary } from '../../../core/models/profile.models';
import {
  DecisionRuleDetail,
  DecisionRuleUpsertPayload,
  RuleCondition,
  RuleConditionType,
  RULE_OPERATORS_BY_VALUE_TYPE,
  RULE_VALUE_TYPE_BY_CONDITION,
  RuleAction
} from '../../../core/models/rule.models';
import {
  ACTION_TYPE_OPTIONS,
  GAME_TYPE_OPTIONS,
  PLAYER_POSITION_OPTIONS,
  SCENARIO_TYPE_OPTIONS,
  SizingType,
  STREET_OPTIONS,
  STRATEGY_LEGEND_OPTIONS
} from '../../../core/models/referentials';
import { ProfilesService } from '../../../core/services/profiles.service';
import { RangesService } from '../../../core/services/ranges.service';
import { RulesService } from '../../../core/services/rules.service';

@Component({
  selector: 'app-rule-detail',
  templateUrl: './rule-detail.component.html',
  styleUrls: ['./rule-detail.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class RuleDetailComponent {
  private readonly destroyRef = inject(DestroyRef);

  readonly ruleId = this.route.snapshot.paramMap.get('id');
  readonly isCreateMode = !this.ruleId;
  readonly profiles$ = this.profilesService.list();
  readonly rule$: Observable<DecisionRuleDetail | null> = this.isCreateMode
    ? of(null)
    : this.route.paramMap.pipe(switchMap((params) => this.rulesService.getById(params.get('id') || '')));

  readonly gameTypes = GAME_TYPE_OPTIONS;
  readonly streets = STREET_OPTIONS;
  readonly scenarioTypes = SCENARIO_TYPE_OPTIONS;
  readonly positions = PLAYER_POSITION_OPTIONS;
  readonly strategyLegends = STRATEGY_LEGEND_OPTIONS;
  readonly actionTypes = ACTION_TYPE_OPTIONS;
  readonly sizingTypes = Object.values(SizingType);

  readonly form = this.fb.group({
    profileId: ['', Validators.required],
    name: ['', [Validators.required, Validators.maxLength(120)]],
    description: [''],
    gameType: [GAME_TYPE_OPTIONS[0], Validators.required],
    street: [STREET_OPTIONS[0], Validators.required],
    scenarioType: [SCENARIO_TYPE_OPTIONS[0], Validators.required],
    heroPosition: [null],
    villainPosition: [null],
    heroHandCode: [''],
    heroStrategyLegend: [null],
    heroRangeSetId: [''],
    villainRangeSetId: [''],
    facingActionType: [null],
    lineSignature: [''],
    sizingConditionCode: [''],
    enabled: [true],
    stopOnMatch: [false],
    priority: [100, [Validators.required]],
    note: [''],
    conditions: this.fb.array([]),
    actions: this.fb.array([])
  });

  heroRangeOptions: Array<{ id: string; name: string }> = [];
  villainRangeOptions: Array<{ id: string; name: string }> = [];

  constructor(
    private readonly fb: UntypedFormBuilder,
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly profilesService: ProfilesService,
    private readonly rangesService: RangesService,
    private readonly rulesService: RulesService,
    private readonly cdr: ChangeDetectorRef
  ) {
    if (this.isCreateMode) {
      this.addCondition();
      this.addAction();
    } else {
      this.rule$.pipe(takeUntilDestroyed(this.destroyRef)).subscribe((rule: DecisionRuleDetail | null) => {
        if (rule) {
          this.patchRule(rule);
          this.cdr.markForCheck();
        }
      });
    }

    this.form.get('profileId')?.valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((profileId: string) => {
      if (!profileId) {
        this.heroRangeOptions = [];
        this.villainRangeOptions = [];
        this.cdr.markForCheck();
        return;
      }

      this.rangesService.listHero(profileId)
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe((ranges) => {
        this.heroRangeOptions = ranges.map((range) => ({ id: range.id, name: range.name }));
        this.cdr.markForCheck();
      });
      this.rangesService.listVillain(profileId)
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe((ranges) => {
        this.villainRangeOptions = ranges.map((range) => ({ id: range.id, name: range.name }));
        this.cdr.markForCheck();
      });
    });
  }

  get conditions(): UntypedFormArray {
    return this.form.get('conditions') as UntypedFormArray;
  }

  get actions(): UntypedFormArray {
    return this.form.get('actions') as UntypedFormArray;
  }

  saveRule(): void {
    if (this.form.invalid || this.actions.length === 0) {
      this.form.markAllAsTouched();
      return;
    }

    const rawValue = this.form.getRawValue();
    const payload: DecisionRuleUpsertPayload = {
      profileId: rawValue.profileId,
      name: rawValue.name,
      description: rawValue.description || null,
      gameType: rawValue.gameType,
      street: rawValue.street,
      scenarioType: rawValue.scenarioType,
      heroPosition: rawValue.heroPosition || null,
      villainPosition: rawValue.villainPosition || null,
      heroHandCode: rawValue.heroHandCode || null,
      heroStrategyLegend: rawValue.heroStrategyLegend || null,
      heroRangeSetId: rawValue.heroRangeSetId || null,
      villainRangeSetId: rawValue.villainRangeSetId || null,
      facingActionType: rawValue.facingActionType || null,
      lineSignature: rawValue.lineSignature || null,
      sizingConditionCode: rawValue.sizingConditionCode || null,
      enabled: !!rawValue.enabled,
      stopOnMatch: !!rawValue.stopOnMatch,
      priority: Number(rawValue.priority),
      note: rawValue.note || null,
      conditions: this.conditions.controls.map((control) => control.getRawValue() as RuleCondition),
      actions: this.actions.controls.map((control) => ({
        ...(control.getRawValue() as RuleAction),
        sizingValue: this.requiresSizingValue(control as UntypedFormGroup)
          ? Number(control.get('sizingValue')?.value)
          : null
      }))
    };

    const request$ = this.isCreateMode
      ? this.rulesService.create(payload)
      : this.rulesService.update(this.ruleId!, payload);

    request$.subscribe((rule) => {
      void this.router.navigate(['/rules', rule.id]);
    });
  }

  deleteRule(): void {
    if (this.isCreateMode || !window.confirm('Supprimer cette regle ?')) {
      return;
    }

    this.rulesService.delete(this.ruleId!).subscribe(() => {
      void this.router.navigate(['/rules']);
    });
  }

  duplicateRule(): void {
    if (this.isCreateMode) {
      return;
    }

    const name = window.prompt('Nom de la copie');
    if (name === null) {
      return;
    }

    this.rulesService.duplicate(this.ruleId!, { name: name || null }).subscribe((rule) => {
      void this.router.navigate(['/rules', rule.id]);
    });
  }

  addCondition(): void {
    const valueType = RULE_VALUE_TYPE_BY_CONDITION[RuleConditionType.HERO_WAS_PREFLOP_AGGRESSOR];
    this.conditions.push(
      this.fb.group({
        conditionType: [RuleConditionType.HERO_WAS_PREFLOP_AGGRESSOR, Validators.required],
        operator: [RULE_OPERATORS_BY_VALUE_TYPE[valueType][0], Validators.required],
        valueType: [valueType, Validators.required],
        expectedValue: ['true', Validators.required],
        secondaryValue: [null],
        conditionOrder: [this.conditions.length + 1, Validators.required]
      })
    );
  }

  removeCondition(index: number): void {
    this.conditions.removeAt(index);
  }

  addAction(): void {
    this.actions.push(
      this.fb.group({
        actionType: [ACTION_TYPE_OPTIONS[0], Validators.required],
        sizingType: [SizingType.CATEGORY, Validators.required],
        sizingValue: [null],
        executionOrder: [this.actions.length + 1, Validators.required],
        messageTemplate: ['']
      })
    );
  }

  removeAction(index: number): void {
    this.actions.removeAt(index);
  }

  requiresSizingValue(group: UntypedFormGroup): boolean {
    const sizingType = group.get('sizingType')?.value as SizingType;
    return sizingType !== SizingType.ALL_IN && sizingType !== SizingType.CATEGORY;
  }

  trackById(_: number, profile: StrategicProfileSummary): string {
    return profile.id;
  }

  private patchRule(rule: DecisionRuleDetail): void {
    this.form.patchValue({
      profileId: rule.profileId,
      name: rule.name,
      description: rule.description || '',
      gameType: rule.gameType,
      street: rule.street,
      scenarioType: rule.scenarioType,
      heroPosition: rule.heroPosition || null,
      villainPosition: rule.villainPosition || null,
      heroHandCode: rule.heroHandCode || '',
      heroStrategyLegend: rule.heroStrategyLegend || null,
      heroRangeSetId: rule.heroRangeSetId || '',
      villainRangeSetId: rule.villainRangeSetId || '',
      facingActionType: rule.facingActionType || null,
      lineSignature: rule.lineSignature || '',
      sizingConditionCode: rule.sizingConditionCode || '',
      enabled: rule.enabled,
      stopOnMatch: rule.stopOnMatch,
      priority: rule.priority,
      note: rule.note || ''
    });

    this.conditions.clear();
    this.actions.clear();

    rule.conditions.forEach((condition) => {
      this.conditions.push(
        this.fb.group({
          conditionType: [condition.conditionType, Validators.required],
          operator: [condition.operator, Validators.required],
          valueType: [condition.valueType, Validators.required],
          expectedValue: [condition.expectedValue, Validators.required],
          secondaryValue: [condition.secondaryValue ?? null],
          conditionOrder: [condition.conditionOrder, Validators.required]
        })
      );
    });

    rule.actions.forEach((action) => {
      this.actions.push(
        this.fb.group({
          actionType: [action.actionType, Validators.required],
          sizingType: [action.sizingType, Validators.required],
          sizingValue: [action.sizingValue ?? null],
          executionOrder: [action.executionOrder, Validators.required],
          messageTemplate: [action.messageTemplate || '']
        })
      );
    });
  }
}
