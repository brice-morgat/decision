import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { ProfilesService } from './profiles.service';
import { ApiHttpService } from './api-http.service';
import { APP_CONFIG } from '../config/app-config';
import { GameType } from '../models/referentials';

describe('ProfilesService', () => {
  let service: ProfilesService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [ProfilesService, ApiHttpService]
    });

    service = TestBed.inject(ProfilesService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('lists profiles', () => {
    service.list().subscribe((profiles) => {
      expect(profiles.length).toBe(1);
      expect(profiles[0].name).toBe('Default');
      expect(profiles[0].gameType).toBe(GameType.CASH);
    });

    const req = httpMock.expectOne(`${APP_CONFIG.apiBaseUrl}/profiles`);
    expect(req.request.method).toBe('GET');
    req.flush([
      {
        id: 'p1',
        name: 'Default',
        description: 'desc',
        gameType: GameType.CASH,
        active: true,
        archived: false,
        versionLabel: 'v1',
        updatedAt: '2026-03-11T00:00:00Z'
      }
    ]);
  });

  it('activates a profile', () => {
    service.activate('p1').subscribe((profile) => {
      expect(profile.active).toBeTrue();
    });

    const req = httpMock.expectOne(`${APP_CONFIG.apiBaseUrl}/profiles/p1/activate`);
    expect(req.request.method).toBe('PUT');
    req.flush({
      id: 'p1',
      name: 'Default',
      description: 'desc',
      gameType: GameType.CASH,
      active: true,
      archived: false,
      versionLabel: 'v1',
      createdAt: '2026-03-10T00:00:00Z',
      updatedAt: '2026-03-11T00:00:00Z'
    });
  });
});
