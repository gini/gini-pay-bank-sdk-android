package net.gini.pay.appscreenapi.di

import net.gini.android.DocumentMetadata
import net.gini.pay.bank.GiniPayBank
import net.gini.pay.bank.ginipayapi.getGiniApi
import net.gini.pay.bank.network.getDefaultNetworkApi
import net.gini.pay.bank.network.getDefaultNetworkService
import org.koin.dsl.module

val giniModule = module {
    single {
        DocumentMetadata().apply {
            setBranchId("GiniBankExampleAndroid")
            add("AppFlow", "ScreenAPI")
        }
    }
    // You need to add clientId and clientSecret properties to: app/src/main/resources/client.properties.
    // Note that resources needs to be java resources folder, not a regular folder.
    single { getDefaultNetworkService(get(), getProperty("clientId"), getProperty("clientSecret"), "example.com", get()) }
    single { getDefaultNetworkApi(get()) }
    single { getGiniApi(get(), getProperty("clientId"), getProperty("clientSecret"), "example.com") }
    single { GiniPayBank.apply { setGiniApi(get()) } }
}