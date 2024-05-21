package com.github.plplmax.planning.timetable

import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore
import ai.timefold.solver.core.api.score.stream.Constraint
import ai.timefold.solver.core.api.score.stream.ConstraintFactory
import ai.timefold.solver.core.api.score.stream.ConstraintProvider
import ai.timefold.solver.core.api.score.stream.Joiners
import com.github.plplmax.planning.lessons.Lesson

class TimetableConstraintProvider : ConstraintProvider {
    override fun defineConstraints(constraintFactory: ConstraintFactory): Array<Constraint>? {
        return arrayOf(
            roomConflict(constraintFactory),
            teacherConflict(constraintFactory),
            groupConflict(constraintFactory),
        )
    }

    private fun roomConflict(constraintFactory: ConstraintFactory): Constraint {
        return constraintFactory
            .forEachUniquePair(
                Lesson::class.java,
                Joiners.equal(Lesson::timeslot),
                Joiners.equal(Lesson::room)
            )
            .penalize(HardSoftScore.ONE_HARD)
            .asConstraint("Room conflict");
    }

    private fun teacherConflict(constraintFactory: ConstraintFactory): Constraint {
        return constraintFactory
            .forEachUniquePair(
                Lesson::class.java,
                Joiners.equal(Lesson::timeslot),
                Joiners.equal(Lesson::teacher)
            )
            .penalize(HardSoftScore.ONE_HARD)
            .asConstraint("Teacher conflict");
    }

    private fun groupConflict(constraintFactory: ConstraintFactory): Constraint {
        return constraintFactory
            .forEachUniquePair(
                Lesson::class.java,
                Joiners.equal(Lesson::timeslot),
                Joiners.equal(Lesson::group)
            )
            .penalize(HardSoftScore.ONE_HARD)
            .asConstraint("Group conflict");
    }
}