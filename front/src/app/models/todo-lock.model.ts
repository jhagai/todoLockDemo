export interface TodoLock {
  id: number;
  version: number;
  endDate: number;
  count: number;
  user: { userId: number, login: string };
}
