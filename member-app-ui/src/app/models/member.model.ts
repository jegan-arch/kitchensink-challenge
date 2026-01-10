export interface MemberRequest {
  userName: string;
  name: string;
  email: string;
  phoneNumber: string;
  role?: string;
}

export interface MemberResponse {
  id: string;
  userName: string;
  name: string;
  email: string;
  phoneNumber: string;
  role: string; 
  createdAt: string;
  updatedAt: string;
  createdBy: string;
  updatedBy: string;
  isPasswordTemporary?: boolean;
}