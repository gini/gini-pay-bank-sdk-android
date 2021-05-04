package net.gini.pay.appscreenapi.di

import net.gini.android.GiniBuilder
import net.gini.pay.bank.pay.GiniBankPay
import org.koin.dsl.module

val giniModule = module {
    single { GiniBuilder(get(), getProperty("clientId"), getProperty("clientSecret"), "example.com").build() }
    single { GiniBankPay(get()) }
}