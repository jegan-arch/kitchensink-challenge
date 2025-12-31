import { Injectable } from '@angular/core';
import { Subject } from 'rxjs';

export interface ToastMessage {
  message: string;
  type: 'success' | 'danger' | 'warning' | 'info';
  id: number; // Unique ID to handle removal correctly
}

@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  
  private notificationSubject = new Subject<ToastMessage>();
  notification$ = this.notificationSubject.asObservable();
  
  private counter = 0;

  showSuccess(message: string) {
    this.notify(message, 'success');
  }

  showError(message: string) {
    this.notify(message, 'danger');
  }

  showWarning(message: string) {
    this.notify(message, 'warning');
  }

  private notify(message: string, type: 'success' | 'danger' | 'warning') {
    this.counter++;
    this.notificationSubject.next({ 
      message, 
      type, 
      id: this.counter 
    });
  }
}