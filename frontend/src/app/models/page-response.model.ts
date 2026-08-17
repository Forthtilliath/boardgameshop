/** Miroir de `com.bgs.boardgameshop.common.PageResponse` cote backend. */
export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}
