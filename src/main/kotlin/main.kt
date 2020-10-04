import model.*
import repo.ListRepo

val persons = ListRepo<Person>()
val courses = ListRepo<Course>()
val taskTypes = ListRepo<Type>()
val topListRepo = ListRepo<Rank>()