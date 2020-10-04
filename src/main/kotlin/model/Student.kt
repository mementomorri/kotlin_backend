package model

class Student (
    name: String,
    val group: String
) : Person(name) {

    fun readGradesAtCourse(courseName: String): Map<String, Int>? {
        return courses.find { it.name == courseName }?.studentGrades(this.name)
//        val grades = courses[courseName].studentGrades(this.name)

    }

    fun readRankAtCourse(courseName: String): Rank{
        return courses.find { it.name == courseName }?.getRankByName(this.name) ?: Rank(this.name, courseName)
    }
}