package model

import persons
import java.time.LocalDate

class Tutor(
    name: String,
    val post: String
) : Person(name) {

    fun setTopList(course: Course){
        course.students.forEach { student ->
            course.topList.add(Rank(student.name,course.name))
        }
    }

    fun readGrades(courseName: String, studentName: String): Map<String, Int>? {
       return courses.find { it.name == courseName }?.studentGrades(studentName)
    }

    fun setGrade(courseName: String, taskName: String, studentName: String, value: Int) {
        val course = courses.find { it.name == courseName }
        if (course != null) {
            course.setGrade(taskName,studentName,value)
            println("Grade has been set!")
        } else{
            println("No such course")
        }
    }

    fun setTask(courseName: String,
                name: String,
                type: Type,
                description: String="",
                maxValue: Int=1,
                deadLine: LocalDate=LocalDate.now()) {
        val course = courses.find { it.name==courseName }
        if (course != null) {
            course.tasks.add(Task(name, type, description, maxValue, deadLine))
            println("Task has been set!")
        } else{
            println("No such course")
        }
    }

    fun setCourse(courseName: String){
        val courseAlreadyExist = courses.find { it.name == courseName } != null
        if (courseAlreadyExist) println("Course with name $courseName already exists") else{
            courses.add(Course(courseName))
            println("Course has been added")
        }
    }

    fun addStudentToCourse(courseName: String, studentName: String){
        val course = courses.find { it.name==courseName }
        val student = persons[studentName] as Student
        if (course != null && student != null){
            course.students.add(student)
            println("Student has been added!")
        } else {
            if (course == null) println("No such course")
            if (student == null) println("No such student")
        }
    }
}