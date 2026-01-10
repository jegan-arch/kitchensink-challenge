import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { finalize } from 'rxjs/operators';
import { AuthService } from '../services/auth.service';
import { NotificationService } from '../services/notification.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent {
  
  loginForm: FormGroup;
  isLoading = false;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private notificationService: NotificationService
  ) {
    this.loginForm = this.fb.group({
      userName: ['', Validators.required],
      password: ['', Validators.required]
    });
  }

  onSubmit(): void {
    if (this.loginForm.invalid) {
      return;
    }

    this.isLoading = true;
    const { userName, password } = this.loginForm.value;

    this.authService.login(userName, password)
      .pipe(
        finalize(() => this.isLoading = false)
      )
      .subscribe({
        next: () => {        
          this.notificationService.showSuccess(`Welcome back, ${userName}!`);
          this.router.navigate(['/dashboard']); 
        },
        error: (err) => {
          const serverMessage = err.error?.message || err.error?.error;

          if (serverMessage) {
             this.notificationService.showError(serverMessage);
          } else if (err.status === 401) {
             this.notificationService.showError("Invalid userName or password.");
          } else if (err.status === 0) {
             this.notificationService.showError("Cannot connect to server. Please check your internet.");
          } else {
             this.notificationService.showError("An unexpected error occurred.");
          }
        }
      });
  }

  isFieldInvalid(field: string): boolean {
    const control = this.loginForm.get(field);
    return !!(control && control.invalid && (control.dirty || control.touched));
  }
}