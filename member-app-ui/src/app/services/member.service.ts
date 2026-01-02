import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { MemberRequest, MemberResponse } from '../models/member.model';

@Injectable({
  providedIn: 'root'
})
export class MemberService {
  
  private readonly API_URL = 'http://localhost:8083/api/member';

  constructor(private http: HttpClient) { }

  getAllMembers(): Observable<MemberResponse[]> {
    return this.http.get<MemberResponse[]>(this.API_URL + "/all");
  }

  getMemberById(id: string): Observable<MemberResponse> {
  return this.http.get<MemberResponse>(`${this.API_URL}/${id}`);
}

  registerMember(member: MemberRequest): Observable<MemberResponse> {
    return this.http.post<MemberResponse>(this.API_URL, member);
  }

  deleteMember(id: string): Observable<void> {
    return this.http.delete<void>(`${this.API_URL}/${id}`);
  }
}