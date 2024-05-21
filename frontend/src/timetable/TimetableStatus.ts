export enum TimetableStatus {
  NOT_SOLVING = 'NOT_SOLVING',
  SOLVING_ACTIVE = 'SOLVING_ACTIVE',
  SOLVING_SCHEDULED = 'SOLVING_SCHEDULED'
}

export interface TimetableStatusResponse {
  status: TimetableStatus
}
