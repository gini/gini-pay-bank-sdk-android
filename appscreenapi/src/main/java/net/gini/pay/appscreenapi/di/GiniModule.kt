package net.gini.pay.appscreenapi.di

import net.gini.android.DocumentMetadata
import net.gini.pay.appscreenapi.R
import net.gini.pay.bank.GiniPayBank
import net.gini.pay.bank.ginipayapi.getGiniApi
import net.gini.pay.bank.network.getDefaultNetworkApi
import net.gini.pay.bank.network.getDefaultNetworkService
import org.koin.android.ext.koin.androidContext
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
    single { getDefaultNetworkService(get(), androidContext().getString(R.string.gini_api_client_id),
        androidContext().getString(R.string.gini_api_client_secret), "example.com", get()) }
    single { getDefaultNetworkApi(get()) }
    single { getGiniApi(get(), androidContext().getString(R.string.gini_api_client_id),
        androidContext().getString(R.string.gini_api_client_secret), "example.com") }
    single { GiniPayBank.apply { setGiniApi(get()) } }
}