package model

import courses
import persons
import repo.Item

class Rank (
        val studentName: String,
        val courseName: String,
        override val name: String = "$studentName's rank"
):Item{
    var rank : Pair<String,Double> = Pair(studentName,0.0)
        get() = calculateRank()
    val student = persons[studentName] as Student
    val course = courses[courseName]
    var weight:Double = 1.0/ (course?.tasks?.size?.toDouble()!!)

    private fun calculateRank():Pair<String,Double>{
        var rankI = 0.0

        if (course != null && student != null) {
            course.tasks.forEach { task ->
                rankI += (weight) * (task.readGrade(student.name)!!.toDouble() / task.maxValue.toDouble())
            }
        }
        return Pair(studentName, String.format("%.2f", rankI).toDouble())
    }
    override fun  toString(): String {
        return "${student.name} got ${(rank.second*100).toInt()}%"
    }
}