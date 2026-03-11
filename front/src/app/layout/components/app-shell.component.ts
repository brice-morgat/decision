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
    { label: 'Profils', hint: 'Configurations de jeu', link: '/profiles' },
    { label: 'Ranges Hero', hint: 'Mains et labels', link: '/ranges/hero' },
    { label: 'Ranges Villain', hint: 'Hypotheses adverses', link: '/ranges/villain' },
    { label: 'Regles', hint: 'Priorites et actions', link: '/rules' },
    { label: 'Historique', hint: 'Reviews et resultats', link: '/reviews' },
    { label: 'Parametres', hint: 'Desktop et preferences', link: '/settings' }
  ];
}
