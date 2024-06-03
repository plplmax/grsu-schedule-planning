import type { Subgroup } from './subgroup'

export interface Division {
  id: number
}

export interface DivisionDetail {
  id: number
  subgroups: Subgroup[]
}
