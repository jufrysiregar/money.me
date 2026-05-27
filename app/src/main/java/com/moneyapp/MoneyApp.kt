package com.moneyapp

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class for Money.me.
 * Annotated with @HiltAndroidApp to trigger Hilt's code generation
 * and set up the application-level dependency injection component.
 */
@HiltAndroidApp
class MoneyApp : Application()
