package model

import repo.Item

class Type (
    override val name: String,
    val shortname: String
) : Item