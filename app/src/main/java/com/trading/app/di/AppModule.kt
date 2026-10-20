package com.trading.app.di

import com.trading.app.data.repository.TradingRepositoryImpl
import com.trading.app.domain.repository.TradingRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindTradingRepository(
        tradingRepositoryImpl: TradingRepositoryImpl
    ): TradingRepository
}
