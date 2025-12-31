import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { MemberService } from '../services/member.service';
import { AuthService } from '../services/auth.service';
import { NotificationService } from '../services/notification.service';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {

  memberForm: FormGroup;
  allMembers: any[] = [];
  paginatedMembers: any[] = [];
  currentUser: string = '';

  currentPage = 1;
  pageSize = 10;
  totalPages = 0;

  isAdmin = false;
  isLoading = false;
  isSubmitting = false;

  constructor(
    private fb: FormBuilder,
    private memberService: MemberService,
    private authService: AuthService,
    private notificationService: NotificationService,
    private router: Router
  ) {
    this.memberForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(2)]],
      email: ['', [Validators.required, Validators.email]],
      phoneNumber: ['', [Validators.required, Validators.pattern(/^[6-9]\d{9}$/)]]
    });
  }

  ngOnInit(): void {
    const user = this.authService.getUser();
    this.currentUser = user ? user.username : 'User';
    this.isAdmin = this.authService.isAdmin();

    this.loadMembers();
  }

  loadMembers(): void {
    this.isLoading = true;
    this.memberService.getAllMembers().subscribe({
      next: (data: any) => {
        this.allMembers = data;
        this.updatePagination();
        this.isLoading = false;
      },
      error: () => this.isLoading = false
    });
  }

  updatePagination(): void {
    this.totalPages = Math.ceil(this.allMembers.length / this.pageSize);
    
    if (this.currentPage > this.totalPages && this.totalPages > 0) {
      this.currentPage = this.totalPages;
    }

    const startIndex = (this.currentPage - 1) * this.pageSize;
    const endIndex = startIndex + this.pageSize;
    
    this.paginatedMembers = this.allMembers.slice(startIndex, endIndex);
  }

  changePage(newPage: number): void {
    if (newPage >= 1 && newPage <= this.totalPages) {
      this.currentPage = newPage;
      this.updatePagination();
    }
  }

  onSubmit(): void {
    if (this.memberForm.invalid) return;

    this.isSubmitting = true;
    this.memberService.registerMember(this.memberForm.value).subscribe({
      next: (newMember: any) => {
        this.notificationService.showSuccess(`Member ${newMember.name} added successfully!`);
        
        this.allMembers.push(newMember);
        this.updatePagination();
        
        this.memberForm.reset();
        this.isSubmitting = false;
      },
      error: () => this.isSubmitting = false
    });
  }

  deleteMember(id: string): void {
    if (!confirm('Are you sure you want to delete this member?')) return;

    this.memberService.deleteMember(id).subscribe({
      next: () => {
        this.notificationService.showSuccess('Member deleted successfully');
        
        this.allMembers = this.allMembers.filter(m => m.id !== id);
        this.updatePagination();
      }
    });
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  isFieldInvalid(field: string): boolean {
    const control = this.memberForm.get(field);
    return !!(control && control.invalid && (control.dirty || control.touched));
  }
}