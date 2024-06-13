package com.github.plplmax.planning.timetable

import ai.timefold.solver.core.api.score.director.ScoreDirector
import ai.timefold.solver.core.impl.heuristic.selector.common.decorator.SelectionFilter
import ai.timefold.solver.core.impl.heuristic.selector.move.generic.SwapMove
import com.github.plplmax.planning.lessons.Lesson

class DifferentSubjectsSwapMoveFilter : SelectionFilter<Timetable, SwapMove<Timetable>> {
    override fun accept(scoreDirector: ScoreDirector<Timetable>, selection: SwapMove<Timetable>): Boolean {
        val leftLesson = selection.leftEntity as Lesson
        val rightLesson = selection.rightEntity as Lesson
        return !(leftLesson.group == rightLesson.group &&
                leftLesson.subgroup == rightLesson.subgroup &&
                leftLesson.subject == rightLesson.subject)
    }
}
