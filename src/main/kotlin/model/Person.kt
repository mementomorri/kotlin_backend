package model

import repo.Item

open class Person(
    override val name: String
) : Item {
    val courses = ArrayList<Course>()
}