import type { Division } from './division'

export interface Subgroup {
  id: number
  name: string
}

export interface SubgroupDetail {
  id: number
  name: string
  division: Division
}
