package ru.secon.core.utils

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update


/**
 * [MutableSet] с возможностью отслеживать изменение значений.
 *
 * @see MutableSet
 */
class ObservableMutableSet<T> : MutableSet<T> {
    private val stateFlow = MutableStateFlow(emptySet<T>())
    private val origin: Set<T> get() = stateFlow.value

    override val size: Int get() = origin.size

    override fun add(element: T): Boolean = if (!contains(element)) {
        mutate { plus(element) }
        true
    } else {
        false
    }

    override fun addAll(elements: Collection<T>): Boolean = if (!containsAll(elements)) {
        mutate { plus(elements) }
        true
    } else {
        false
    }

    override fun remove(element: T): Boolean = if (contains(element)) {
        mutate { minus(element) }
        true
    } else {
        false
    }

    override fun removeAll(elements: Collection<T>): Boolean = if (containsAll(elements)) {
        mutate { minus(elements.toSet()) }
        true
    } else {
        false
    }

    override fun clear() = mutate { emptySet() }

    override fun isEmpty(): Boolean = origin.isEmpty()

    override fun containsAll(elements: Collection<T>): Boolean = origin.containsAll(elements)

    override fun contains(element: T): Boolean = origin.contains(element)

    override fun iterator(): MutableIterator<T> = origin.toMutableSet().iterator()

    override fun retainAll(elements: Collection<T>): Boolean {
        val origin = origin
        val retained = origin.filter { it in elements }.toSet()
        return if(retained.size == origin.size) {
            false
        } else {
            mutate { retained }
            true
        }
    }

    /** Отслеживает изменение. */
    fun observe(): StateFlow<Set<T>> = stateFlow.asStateFlow()

    private fun mutate(block: Set<T>.() -> Set<T>) = stateFlow.update { block(it) }
}
