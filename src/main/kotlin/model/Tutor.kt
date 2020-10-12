package model

import persons
import java.time.LocalDate

class Tutor(
    name: String,
    val post: String
) : Person(name) {}