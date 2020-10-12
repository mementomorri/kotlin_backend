package model

import repo.Item
import java.time.LocalDate

class Task(
    override val name: String,
    val type: Type,
    val description: String = "",
    val maxValue: Int = 1,
    val deadline: LocalDate = LocalDate.now()
) : Item {
    val grades = ArrayList<Grade>()

    fun getGrade(studentName: String): Int?{
        return grades
                .filter { it.student.name == studentName }
                .maxByOrNull { it.value }
                ?.value ?: 0
    }

    companion object Factory{
        fun getType(type: Type):
                Task{
            return when(type){
                Type.LECTURE -> Task("Lecture",Type.LECTURE)
                Type.LABORATORY -> Task("Laboratory",Type.LABORATORY)
                Type.TEST -> Task("Test",Type.TEST)
                Type.PERSONAL_PROJECT -> Task("Personal project",Type.PERSONAL_PROJECT)
            }
        }
    }
}