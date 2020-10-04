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

    fun readGrade(studentName: String): Int?{
        return grades
                .filter { it.student.name == studentName }
                .maxByOrNull { it.value }
                ?.value ?: 0
    }
}