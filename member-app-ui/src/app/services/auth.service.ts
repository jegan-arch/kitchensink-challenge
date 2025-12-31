import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { Router } from '@angular/router';
import { jwtDecode } from 'jwt-decode';

// 1. Strong Typing (No more 'any')
export interface User {
  username: string;
  roles: string[];
  token: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  
  // Ideally, this comes from environment.ts
  private readonly AUTH_API = 'http://localhost:8082/auth/';
  private readonly STORAGE_KEY = 'memberhub_user';

  // 2. Reactive State (The "Source of Truth")
  private currentUserSubject: BehaviorSubject<User | null>;
  public currentUser$: Observable<User | null>;

  constructor(private http: HttpClient, private router: Router) {
    const storedUser = localStorage.getItem(this.STORAGE_KEY);
    this.currentUserSubject = new BehaviorSubject<User | null>(storedUser ? JSON.parse(storedUser) : null);
    this.currentUser$ = this.currentUserSubject.asObservable();
  }

  login(username: string, password: string): Observable<User> {
    return this.http.post<User>(this.AUTH_API + 'login', { username, password })
      .pipe(map(user => {
        localStorage.setItem(this.STORAGE_KEY, JSON.stringify(user));
        this.currentUserSubject.next(user);
        
        return user;
      }));
  }

  public isLoggedIn(): boolean {
    const token = this.getJwtToken();
    if (!token) {
      return false;
    }

    if (this.isTokenExpired(token)) {
      this.logout();
      return false;
    }

    return true;
  }

  private isTokenExpired(token: string): boolean {
    try {
      const decoded: any = jwtDecode(token);
      const expirationDate = decoded.exp * 1000;
      const currentDate = Date.now();
      return currentDate > expirationDate; 
    } catch (error) {
      return true;
    }
  }

  logout(): void {
    localStorage.removeItem(this.STORAGE_KEY);
    this.currentUserSubject.next(null);
    this.router.navigate(['/login']);
  }

  public getUser(): User | null {
    return this.currentUserSubject.value;
  }

  public getJwtToken(): string | null {
    return this.getUser()?.token || null;
  }

  public hasRole(role: string): boolean {
    const user = this.getUser();
    return user ? user.roles.includes(role) : false;
  }

  public isAdmin(): boolean {
    return this.hasRole('ROLE_ADMIN');
  }
}