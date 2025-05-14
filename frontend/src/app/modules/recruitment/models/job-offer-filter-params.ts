export interface JobOfferFilterParams {
  page?: number;
  size?: number;
  sortBy?: string;
  sortDir?: 'asc' | 'desc';
  title?: string;
  department?: string;
  role?: string;
  isInternal?: boolean;
  status?: 'OPEN' | 'CLOSED' | string;
}
