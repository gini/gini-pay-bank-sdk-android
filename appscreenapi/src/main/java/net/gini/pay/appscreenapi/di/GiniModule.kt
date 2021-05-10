package net.gini.pay.appscreenapi.di

import net.gini.pay.bank.GiniPayBank
import net.gini.pay.bank.ginipayapi.getGiniApi
import org.koin.dsl.module

val giniModule = module {
    single { getGiniApi(get(), getProperty("clientId"), getProperty("clientSecret"), "example.com") }
    single { GiniPayBank.apply { setGiniApi(get()) } }
}