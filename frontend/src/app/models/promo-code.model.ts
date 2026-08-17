export interface PromoCodeResponse {
  code: string;
  discountPercent: number;
}

export interface AdminPromoCodeRequest {
  code: string;
  discountPercent: number;
  active: boolean;
  expiresAt: string | null;
  maxUses: number | null;
}

export interface AdminPromoCodeResponse extends AdminPromoCodeRequest {
  id: number;
  usesCount: number;
}
