package com.trading.app.di

import com.trading.app.data.remote.BinanceApi
import com.trading.app.data.repository.TradingRepositoryImpl
import com.trading.app.domain.repository.TradingRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideBinanceApi(): BinanceApi {
        return Retrofit.Builder()
            .baseUrl(BinanceApi.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(BinanceApi::class.java)
    }

    @Provides
    @Singleton
    fun provideTradingRepository(api: BinanceApi): TradingRepository {
        return TradingRepositoryImpl(api)
    }
}
