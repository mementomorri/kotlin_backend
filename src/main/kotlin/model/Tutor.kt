package model
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import repo.PersonTable

@Serializable
class Tutor(
    override val name: String,
    val post: String,
    override var id: Int=-1
) : Person {
    override fun toString(): String {
        return "Tutor with id=$id, name=$name, post=$post"
    }
}

class TutorTable : PersonTable<Tutor>() {
    val name = varchar("name", 255)
    val post = varchar("post", 50)

    override fun fill(builder: UpdateBuilder<Int>, person: Tutor) {
        builder[name] = person.name
        builder[post] = person.post
    }
    override fun readResult(result: ResultRow) =
            Tutor(
                    result[name],
                    result[post],
                    result[id].value
            )
}

val tutorTable = TutorTable()