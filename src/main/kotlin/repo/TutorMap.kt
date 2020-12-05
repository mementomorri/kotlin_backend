package repo

import model.Student
import model.Tutor

class TutorMap: Repo<Tutor>{
    private var maxId= 0
    private val _repo= HashMap<Int, Tutor>()

    override fun create(element: Tutor) =
            if (_repo.containsValue(element))
                false
            else {
                val newId = maxId++
                element.id = newId
                _repo[newId] = element
                true
            }

    override fun read(id: Int) =
            _repo[id]

    override fun read() =
            _repo.values.toList()

    override fun update(id: Int, element: Tutor) =
            if (_repo.containsKey(id)) {
                _repo[id] = element
                true
            } else
                false

    override fun delete(id: Int) =
            if (_repo.containsKey(id)) {
                _repo.remove(id)
                true
            } else
                false
}