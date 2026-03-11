import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { DecisionEngineService } from './decision-engine.service';
import { ApiHttpService } from './api-http.service';
import { DecisionRequest } from '../models/decision.models';
import {
  ActionType,
  DecisionStatus,
  PlayerPosition,
  ScenarioType,
  Street
} from '../models/referentials';
import { APP_CONFIG } from '../config/app-config';

describe('DecisionEngineService', () => {
  let service: DecisionEngineService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [DecisionEngineService, ApiHttpService]
    });

    service = TestBed.inject(DecisionEngineService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('posts decision request and returns result', () => {
    const request: DecisionRequest = {
      strategyProfileId: 'profile-1',
      heroPosition: PlayerPosition.BTN,
      villainPosition: PlayerPosition.BB,
      scenarioType: ScenarioType.OPEN_FIRST_IN,
      street: Street.PREFLOP,
      effectiveStackInBigBlinds: 100,
      potSizeInBigBlinds: 2.5,
      heroCards: ['As', 'Kh'],
      boardCards: [],
      actionEvents: []
    };

    service.evaluate(request).subscribe((result) => {
      expect(result.status).toBe(DecisionStatus.SUCCESS);
      expect(result.recommendedAction).toBe(ActionType.CALL);
      expect(result.matchedRuleName).toBe('BTN open');
    });

    const req = httpMock.expectOne(`${APP_CONFIG.apiBaseUrl}/decision-assistant/decide`);
    expect(req.request.method).toBe('POST');
    req.flush({
      status: DecisionStatus.SUCCESS,
      recommendedAction: ActionType.CALL,
      recommendedSizingValue: null,
      matchedRuleId: 'rule-1',
      matchedRuleName: 'BTN open',
      explanation: 'Rule matched.',
      trace: ['ok'],
      warnings: [],
      matchedCandidates: []
    });
  });
});
