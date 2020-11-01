package model
import tutors
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import repo.Item
import repo.ItemTable

class Tutor(
    val name: String,
    val post: String,
    override var id: Int=-1
) : Item {}

class TutorTable : ItemTable<Tutor>() {
    val name = varchar("name", 255)
    val post = varchar("post", 50)
    override fun fill(builder: UpdateBuilder<Int>, item: Tutor) {
        builder[name] = item.name
        builder[post] = item.post
    }
    override fun readResult(result: ResultRow) =
            Tutor(
                    result[name],
                    result[post],
                    result[id].value
            )
}

val tutorTable = TutorTable()