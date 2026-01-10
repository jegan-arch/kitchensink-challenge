import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { Router } from '@angular/router';
import { jwtDecode } from 'jwt-decode';

export interface User {
  id: string;
  userName: string;
  email: string;
  role: string;
  token: string;
  isPasswordTemporary?: boolean;
}

interface JwtPayload {
  exp: number;
  iat: number;
  sub: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private readonly AUTH_API = 'http://localhost:8083/api/v1/auth/';
  private readonly STORAGE_KEY = 'memberhub_user';

  private currentUserSubject: BehaviorSubject<User | null>;
  public currentUser$: Observable<User | null>;

  constructor(private http: HttpClient, private router: Router) {
    const storedUser = localStorage.getItem(this.STORAGE_KEY);
    this.currentUserSubject = new BehaviorSubject<User | null>(storedUser ? JSON.parse(storedUser) : null);
    this.currentUser$ = this.currentUserSubject.asObservable();
  }

  login(userName: string, password: string): Observable<User> {
    return this.http.post<User>(this.AUTH_API + 'login', { userName, password })
      .pipe(map(user => {
        localStorage.setItem(this.STORAGE_KEY, JSON.stringify(user));
        this.currentUserSubject.next(user);
        return user;
      }));
  }

  public isLoggedIn(): boolean {
    const token = this.getJwtToken();
    if (!token) return false;

    if (this.isTokenExpired(token)) {
      this.logout();
      return false;
    }
    return true;
  }

  private isTokenExpired(token: string): boolean {
    try {
      const decoded = jwtDecode<JwtPayload>(token);
      return Date.now() > decoded.exp * 1000;
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

  public getRole(): string {
    const user = this.getUser();
    return user?.role || 'USER';
  }

  public hasRole(requiredRole: string): boolean {
    const user = this.getUser();
    return user ? user.role === requiredRole : false;
  }

  public isAdmin(): boolean {
    return this.hasRole('ROLE_ADMIN');
  }
}