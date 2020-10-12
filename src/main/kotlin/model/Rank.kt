package model

import courses
import persons
import repo.Item

class Rank (
        studentName: String,
        courseName: String,
        override val name: String = "$studentName's rank"
):Item{
    val rank
        get() = calculateRank()
    val student = persons[studentName] as Student
    val course = courses[courseName] as Course

    private fun calculateRank():Double{
        var rankI = 0.0
        if (course != null && student != null) {
            course.tasks.forEach { task ->
                rankI += (calculateWeight(task.type)) * (task.getGrade(student.name)!!.toDouble() / task.maxValue.toDouble())
            }
        }
        return String.format("%.2f", rankI).toDouble()
    }

    private fun calculateWeight(type: Type):Double{
        return when(type){
            Type.LECTURE -> 0.1
            Type.LABORATORY -> 0.2
            Type.TEST -> 0.3
            Type.PERSONAL_PROJECT -> 0.4
        }
    }

    override fun  toString(): String {
        return "${student.name} got ${(rank*100).toInt()}%"
    }
}