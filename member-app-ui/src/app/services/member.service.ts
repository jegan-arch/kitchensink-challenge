import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { MemberRequest, MemberResponse } from '../models/member.model';

interface MessageResponse {
  message: string
}

@Injectable({
  providedIn: 'root'
})
export class MemberService {
  
  private readonly API_URL = 'http://localhost:8083/api/v1/members';
  private readonly AUTH_URL = 'http://localhost:8083/api/v1/auth';

  constructor(private http: HttpClient) { }

  getAllMembers(): Observable<MemberResponse[]> {
    return this.http.get<MemberResponse[]>(this.API_URL);
  }

  getMemberById(id: string): Observable<MemberResponse> {
    return this.http.get<MemberResponse>(`${this.API_URL}/${id}`);
  }

  getMyProfile(): Observable<MemberResponse> {
    return this.http.get<MemberResponse>(`${this.API_URL}/me`);
  }

  registerMember(member: MemberRequest): Observable<MemberResponse> {
    return this.http.post<MemberResponse>(`${this.AUTH_URL}/signup`, member);
  }

  updateMember(id: string, member: MemberRequest): Observable<MemberResponse> {
    return this.http.put<MemberResponse>(`${this.API_URL}/${id}`, member);
  }

  changePassword(data: any, id: string): Observable<MessageResponse> {
    return this.http.put<MessageResponse>(`${this.API_URL}/${id}/change-password`, data);
  }

  deleteMember(id: string): Observable<MessageResponse> {
    return this.http.delete<MessageResponse>(`${this.API_URL}/${id}`);
  }
}