package ru.secon.ui.di

import org.koin.dsl.module
import ru.secon.ui.auth.AuthViewModel
import ru.secon.ui.auth.admin.AdminAuthViewModel
import ru.secon.ui.tasks.TaskViewModel

val uiModule = module {
    factory {
        AuthViewModel(get())
    }
    factory {
        TaskViewModel(get())
    }
    factory {
        AdminAuthViewModel(get())
    }
}