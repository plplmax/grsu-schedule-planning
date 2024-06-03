package com.github.plplmax.planning.timetable

import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore
import ai.timefold.solver.core.api.solver.SolutionManager
import ai.timefold.solver.core.api.solver.SolverManager
import ai.timefold.solver.core.config.solver.SolverConfig
import com.github.plplmax.planning.lessons.Lessons
import com.github.plplmax.planning.plugins.routing.AppRoute
import com.github.plplmax.planning.plugins.routing.AppRouteBasic
import com.github.plplmax.planning.subjects.Subjects
import com.github.plplmax.planning.subjects.paired.PairedSubjectsCollection
import com.github.plplmax.planning.timeslots.Timeslots
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.future.asCompletableFuture
import kotlinx.coroutines.launch

class TimetableRoute(
    private val lessons: Lessons,
    private val timeslots: Timeslots,
    private val pairedSubjects: PairedSubjectsCollection,
    private val subjects: Subjects,
    private val solver: SolverManager<Timetable, Int> = SolverManager.create(config),
    vararg children: AppRoute
) : AppRouteBasic(*children) {
    private val scope = CoroutineScope(Dispatchers.IO)
    override fun install(parent: Route) {
        parent.route("/timetable") {
            super.install(this)
            post("/solve") {
                solver.solveAndListen(
                    PROBLEM_ID,
                    Timetable(
                        timeslots = timeslots.all(),
                        lessons = lessons.all(),
                        pairedSubjects = pairedSubjects.all(),
                        subjects = subjects.allDetails(),
                        score = HardSoftScore.ZERO
                    )
                ) { timetable ->
                    scope.launch {
                        lessons.update(timetable.lessons)
                    }.asCompletableFuture().join()
                }
                call.respond(HttpStatusCode.OK)
            }
            post("/stopSolving") {
                solver.terminateEarly(PROBLEM_ID)
                call.respond(HttpStatusCode.OK)
            }
            get("/status") {
                call.respond(TimetableStatus(solver.getSolverStatus(PROBLEM_ID)))
            }
            get {
                SolutionManager.create(solver).explain(
                    Timetable(
                        timeslots = timeslots.all(),
                        lessons = lessons.all(),
                        pairedSubjects = pairedSubjects.all(),
                        subjects = subjects.allDetails(),
                        score = HardSoftScore.ZERO
                    )
                ).also { call.respond(it.toString()) }
            }
        }
    }

    companion object {
        private const val PROBLEM_ID = 1
        private val config = SolverConfig.createFromXmlResource("solver.xml")
    }
}
