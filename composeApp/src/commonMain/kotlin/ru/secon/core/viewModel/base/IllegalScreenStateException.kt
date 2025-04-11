package ru.secon.core.viewModel.base


/**
 * Исключение, возникающее при попытке работать с неверным инстансом состояния
 */
class IllegalScreenStateException(override val message: String?) : Exception()