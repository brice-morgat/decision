import { ChangeDetectionStrategy, Component } from '@angular/core';

@Component({
  selector: 'app-shell',
  templateUrl: './app-shell.component.html',
  styleUrls: ['./app-shell.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class AppShellComponent {
  readonly navItems = [
    { label: 'Assistant', hint: 'Decision temps reel', link: '/assistant' },
    { label: 'Spots', hint: 'Configuration orientee spot', link: '/spots' },
    { label: 'Profils', hint: 'Configurations de jeu', link: '/profiles' },
    { label: 'Ranges Hero', hint: 'Edition avancee', link: '/ranges/hero' },
    { label: 'Ranges Villain', hint: 'Edition avancee', link: '/ranges/villain' },
    { label: 'Regles', hint: 'Priorites et actions', link: '/rules' },
    { label: 'Historique', hint: 'Reviews et resultats', link: '/reviews' },
    { label: 'Parametres', hint: 'Desktop et preferences', link: '/settings' }
  ];
}
