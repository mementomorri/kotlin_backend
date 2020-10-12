package model

import persons
import repo.Item
import toplistRepo
import java.time.LocalDate

class Course(
    override val name: String
) : Item {
    val tutors = ArrayList<Tutor>()
    val students = ArrayList<Student>()
    val tasks = ArrayList<Task>()
    val toplist = ArrayList<Rank>()

    fun setToplist(){
        this.students.forEach{ student ->
            val currentRank= Rank(student.name, this.name)
            this.toplist.add(currentRank)
            toplistRepo.add(currentRank)
        }
    }

    fun printTopList(){
        println("${this.name} toplist:")
        val sortedTopList = toplist.sortedBy { it.rank }
        sortedTopList.forEach { rank ->
            println(rank.toString())
        }
    }

    fun setGrade(taskName: String, studentName: String, value: Int, date: LocalDate = LocalDate.now()) {
        val task = tasks.find { it.name == taskName } ?: return
        val student = students.find { it.name == studentName } ?: return
        if (value !in 0..task.maxValue) return
        val grade = Grade(value, date, student)
        task.grades += grade
    }


    fun studentGrades(studentName: String) =
        tasks.map { task ->
            val value = task.grades
                .filter { it.student.name == studentName }
                .maxByOrNull { it.value }
                ?.value ?: 0
            task.name to value
        }.toMap()

    fun getStudent(studentName: String): Student?{
        return students.firstOrNull{it.name==studentName}
    }

    fun getRankByName(studentName: String):Rank?{
        return toplist.firstOrNull { it.student.name == studentName}
    }

    fun getTask(taskName:String): Task? {
        return tasks.firstOrNull { it.name == taskName }
    }

}