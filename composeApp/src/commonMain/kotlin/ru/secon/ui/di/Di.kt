package ru.secon.ui.di

import org.koin.dsl.module
import ru.secon.ui.auth.admin.AdminAuthViewModel
import ru.secon.ui.auth.profile.ProfileViewModel
import ru.secon.ui.auth.usual.AuthViewModel
import ru.secon.ui.tasks.create.CreateTaskViewModel
import ru.secon.ui.tasks.info.TaskInfoViewModel
import ru.secon.ui.tasks.list.TaskViewModel

val uiModule = module {
    factory {
        AuthViewModel(get(), get())
    }
    factory {
        TaskViewModel(get(),get(),get())
    }
    factory {
        AdminAuthViewModel(get())
    }
    factory {
        TaskInfoViewModel(get(), get(), get(), get())
    }
    factory {
        CreateTaskViewModel(get(), get())
    }
    factory {
        ProfileViewModel(get())
    }
}