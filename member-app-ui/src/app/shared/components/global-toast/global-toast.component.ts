import { Component, OnInit } from '@angular/core';
import { NotificationService, ToastMessage } from 'src/app/services/notification.service';

@Component({
  selector: 'app-global-toast',
  templateUrl: './global-toast.component.html'
})
export class GlobalToastComponent implements OnInit {

  toasts: ToastMessage[] = [];

  constructor(private notificationService: NotificationService) {}

  ngOnInit(): void {
    this.notificationService.notification$.subscribe(toast => {
      this.toasts.push(toast);
      setTimeout(() => this.removeToast(toast.id), 5000);
    });
  }

  removeToast(id: number) {
    this.toasts = this.toasts.filter(t => t.id !== id);
  }
}