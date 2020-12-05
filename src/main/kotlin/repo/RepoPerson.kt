package repo

import model.Person
import model.Student
import model.Tutor

interface RepoPerson<T:Person> {

    fun create(element: T): Boolean // null if element was in repo

    fun read(id: Int): T? // null if id is absent

    fun read(): List<T> // read all

    fun update(id: Int, element: T): Boolean // false if id is absent

    fun delete(id: Int): Boolean // false if id is absent

    fun getStudentsByGroup(group: String): List<Student>? // null if that's not a student repo

    fun getTutorsByPost(post: String): List<Tutor>? // null if that's not a tutor repo
}