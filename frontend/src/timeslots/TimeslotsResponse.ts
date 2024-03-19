import type { TimeslotResponse } from "./TimeslotResponse";

export interface TimeslotsResponse {
  [date: string]: TimeslotResponse[]
}
