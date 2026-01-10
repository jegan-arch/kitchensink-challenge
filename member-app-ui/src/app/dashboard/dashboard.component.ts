import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';
import { finalize } from 'rxjs/operators';
import { MemberService } from '../services/member.service';
import { AuthService } from '../services/auth.service';
import { NotificationService } from '../services/notification.service';
import { MemberResponse } from '../models/member.model';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {

  memberForm: FormGroup;
  passwordForm: FormGroup;

  allMembers: MemberResponse[] = [];
  paginatedMembers: MemberResponse[] = [];

  currentUser: any = null;
  currentRole: string = 'USER';
  isAdmin: boolean = false;

  availableRoles: { label: string, value: string }[] = [
    { label: 'Administrator', value: 'ROLE_ADMIN' },
    { label: 'Standard User', value: 'ROLE_USER' }
  ];

  currentPage = 1;
  pageSize = 10;
  totalPages = 0;

  isLoading = false;
  isSubmitting = false;

  isEditMode = false;
  editingId: string | null = null;

  selectedMember: MemberResponse | null = null;

  showMemberModal: boolean = false;
  showDetailModal: boolean = false;
  showPasswordModal: boolean = false;
  isForceChange = false;

  constructor(
    private fb: FormBuilder,
    private memberService: MemberService,
    private authService: AuthService,
    private notificationService: NotificationService
  ) {
    this.memberForm = this.fb.group({
      userName: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(20), Validators.pattern(/^[a-zA-Z0-9_-]+$/)]],
      name: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(25)]],
      email: ['', [Validators.required, Validators.email]],
      phoneNumber: ['', [Validators.required, Validators.pattern(/^[6-9]\d{9}$/)]],
      role: ['ROLE_USER', Validators.required]
    });

    this.passwordForm = this.fb.group({
      oldPassword: ['', Validators.required],
      newPassword: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', Validators.required]
    }, { validators: this.passwordMatchValidator });
  }

  ngOnInit(): void {
    this.currentUser = this.authService.getUser();
    this.currentRole = this.authService.getRole();
    this.isAdmin = this.currentRole === 'ROLE_ADMIN';

    if (this.currentUser?.isPasswordTemporary) {
      this.isForceChange = true;
      this.openPasswordModal();
      return;
    }

    if (this.isAdmin) {
      this.loadMembers();
    } else {
      this.loadMyProfile();
    }
  }

  openAddMemberModal(): void {
    this.isEditMode = false;
    this.editingId = null;
    this.memberForm.reset({ role: 'ROLE_USER' });
    this.memberForm.get('userName')?.enable();
    this.showMemberModal = true;
  }

  openEditMemberModal(member: MemberResponse): void {
    this.isEditMode = true;
    this.editingId = member.id;

    this.memberForm.patchValue({
      userName: member.userName,
      name: member.name,
      email: member.email,
      phoneNumber: member.phoneNumber,
      role: member.role || 'ROLE_USER'
    });

    this.memberForm.get('userName')?.disable();
    this.showMemberModal = true;
  }

  closeMemberModal(): void {
    this.showMemberModal = false;
    this.memberForm.reset();
  }

  onSubmit(): void {
    if (this.memberForm.invalid) return;

    this.isSubmitting = true;
    const formData = this.memberForm.getRawValue();

    if (this.isEditMode && this.editingId) {
      this.memberService.updateMember(this.editingId, formData)
        .pipe(finalize(() => this.isSubmitting = false))
        .subscribe({
          next: (updatedMember) => {
            if (this.currentUser && updatedMember.id === this.currentUser.id && updatedMember.role !== this.currentRole) {
              this.notificationService.showSuccess('Your role has changed. Please login again.');
              setTimeout(() => this.authService.logout(), 1500);
              return;
            }

            this.notificationService.showSuccess(`Member updated successfully!`);
            const index = this.allMembers.findIndex(m => m.id === updatedMember.id);
            if (index !== -1) {
              this.allMembers[index] = updatedMember;
            }

            if (this.selectedMember && this.selectedMember.id === updatedMember.id) {
              this.selectedMember = updatedMember;
            }

            this.updatePagination();
            this.closeMemberModal();
          }
        });

    } else {
      this.memberService.registerMember(formData)
        .pipe(finalize(() => this.isSubmitting = false))
        .subscribe({
          next: (newMember) => {
            this.notificationService.showSuccess(`User ${formData.userName} created!`);
            this.allMembers.unshift(newMember);
            this.updatePagination();
            this.closeMemberModal();
          }
        });
    }
  }

  deleteMember(member: MemberResponse): void {
    let message = 'Are you sure you want to delete this user?';

    if (this.currentUser && member.id === this.currentUser.id) {
      message = '⚠️ You are about to delete YOUR OWN account. You will be logged out immediately. Are you sure?';
    } else if (member.role === 'ROLE_ADMIN') {
      message = '⚠️ CRITICAL WARNING: You are about to delete an ADMINISTRATOR. This action cannot be undone. Are you absolutely sure?';
    }

    if (!confirm(message)) return;

    this.memberService.deleteMember(member.id)
      .subscribe({
        next: () => {
          if (this.currentUser && member.id === this.currentUser.id) {
            this.notificationService.showSuccess('Your account has been deleted. Logging out...');
            setTimeout(() => {
              this.authService.logout();
            }, 1500);
            return;
          }

          this.notificationService.showSuccess('Deleted successfully');
          this.allMembers = this.allMembers.filter(m => m.id !== member.id);
          this.updatePagination();
        }
      });
  }

  canModify(targetMember: MemberResponse): boolean {
    if (this.currentUser && targetMember.userName === this.currentUser.userName) {
      return true;
    }
    return this.isAdmin;
  }

  loadMembers(): void {
    this.isLoading = true;
    this.memberService.getAllMembers()
      .pipe(finalize(() => this.isLoading = false))
      .subscribe({
        next: (data) => {
          this.allMembers = data;
          this.updatePagination();
        }
      });
  }

  loadMyProfile(): void {
    this.isLoading = true;
    this.memberService.getMyProfile()
      .pipe(finalize(() => this.isLoading = false))
      .subscribe({
        next: (data) => { this.selectedMember = data; }
      });
  }

  updatePagination(): void {
    this.totalPages = Math.ceil(this.allMembers.length / this.pageSize);
    if (this.currentPage > this.totalPages && this.totalPages > 0) this.currentPage = this.totalPages;
    const startIndex = (this.currentPage - 1) * this.pageSize;
    this.paginatedMembers = this.allMembers.slice(startIndex, startIndex + this.pageSize);
  }

  changePage(newPage: number): void {
    if (newPage >= 1 && newPage <= this.totalPages) {
      this.currentPage = newPage;
      this.updatePagination();
    }
  }

  viewDetails(id: string): void {
    this.memberService.getMemberById(id).subscribe({
      next: (data) => {
        this.selectedMember = data;
        this.showDetailModal = true;
      }
    });
  }

  closeDetailModal(): void {
    this.showDetailModal = false;
    setTimeout(() => this.selectedMember = null, 200);
  }

  openPasswordModal(): void {
    this.showPasswordModal = true;
    this.passwordForm.reset();
  }

  closePasswordModal(): void {
    if (this.isForceChange) return;
    this.showPasswordModal = false;
  }

  onChangePassword(): void {
    if (this.passwordForm.invalid) return;

    this.isSubmitting = true;
    const { oldPassword, newPassword } = this.passwordForm.value;

    this.memberService.changePassword({ oldPassword, newPassword }, this.currentUser?.id)
      .pipe(finalize(() => this.isSubmitting = false))
      .subscribe({
        next: () => {
          this.notificationService.showSuccess("Password changed. Please login again.");
          setTimeout(() => {
            this.authService.logout();
          }, 1000);

          if (this.isForceChange) {
            this.showPasswordModal = false;
          } else {
            this.closePasswordModal();
          }
        }
      });
  }

  getRoleBadgeClass(role: string): string {
    if (role === 'ROLE_ADMIN') return 'bg-danger';
    return 'bg-primary';
  }

  isFieldInvalid(field: string): boolean {
    const control = this.memberForm.get(field);
    return !!(control && control.invalid && (control.dirty || control.touched));
  }

  passwordMatchValidator: ValidatorFn = (control: AbstractControl): ValidationErrors | null => {
    const newPass = control.get('newPassword');
    const confirmPass = control.get('confirmPassword');
    return newPass && confirmPass && newPass.value !== confirmPass.value
      ? { mismatch: true }
      : null;
  };
}