package com.adjarabet.user

import android.app.Application
import android.content.Context
import ru.terrakok.cicerone.Cicerone
import ru.terrakok.cicerone.Router

class WordGameApplication : Application() {

    lateinit var cicerone: Cicerone<Router>


    companion object {

        lateinit var wordGameApplication: WordGameApplication
    }

    override fun onCreate() {
        super.onCreate()

        wordGameApplication = this
        this.initCicerone()
    }

    private fun initCicerone(){
        this.cicerone = Cicerone.create()
    }
}