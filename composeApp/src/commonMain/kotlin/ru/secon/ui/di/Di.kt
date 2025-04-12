package ru.secon.ui.di

import org.koin.dsl.module
import ru.secon.ui.auth.usual.AuthViewModel
import ru.secon.ui.auth.admin.AdminAuthViewModel
import ru.secon.ui.tasks.list.TaskViewModel

val uiModule = module {
    factory {
        AuthViewModel(get(), get())
    }
    factory {
        TaskViewModel(get())
    }
    factory {
        AdminAuthViewModel(get())
    }
}