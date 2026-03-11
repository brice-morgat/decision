import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { RangesService } from './ranges.service';
import { ApiHttpService } from './api-http.service';
import { APP_CONFIG } from '../config/app-config';
import { GameType, PlayerPosition, ScenarioType, Street } from '../models/referentials';

describe('RangesService', () => {
  let service: RangesService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [RangesService, ApiHttpService]
    });

    service = TestBed.inject(RangesService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('lists hero ranges for a profile', () => {
    service.listHero('profile-1').subscribe((ranges) => {
      expect(ranges.length).toBe(1);
      expect(ranges[0].heroPosition).toBe(PlayerPosition.BTN);
    });

    const req = httpMock.expectOne(`${APP_CONFIG.apiBaseUrl}/ranges/hero?profileId=profile-1`);
    expect(req.request.method).toBe('GET');
    req.flush([
      {
        id: 'h1',
        profileId: 'profile-1',
        name: 'BTN open',
        description: '',
        gameType: GameType.CASH,
        street: Street.PREFLOP,
        heroPosition: PlayerPosition.BTN,
        scenarioType: ScenarioType.OPEN_FIRST_IN,
        subScenarioCode: null,
        enabled: true,
        priority: 0,
        updatedAt: '2026-03-11T10:00:00Z'
      }
    ]);
  });
});
