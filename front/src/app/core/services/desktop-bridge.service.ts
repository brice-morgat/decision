import { Injectable } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class DesktopBridgeService {
  // Placeholder for desktop wrapper integration (IPC, file system bridge, etc.)
  isDesktop(): boolean {
    return true;
  }
}