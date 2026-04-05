import { DOCUMENT } from '@angular/common';
import { Inject, Injectable } from '@angular/core';

export type AppTheme = 'midnight-blue-red' | 'obsidian-crimson';

@Injectable({
  providedIn: 'root'
})
export class ThemeService {
  readonly defaultTheme: AppTheme = 'midnight-blue-red';

  constructor(@Inject(DOCUMENT) private readonly document: Document) {}

  applyTheme(theme: AppTheme = this.defaultTheme): void {
    this.document.documentElement.setAttribute('data-theme', theme);
  }
}
