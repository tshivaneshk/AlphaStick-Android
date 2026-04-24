package com.example.alphastick.di

import android.app.Application
import androidx.room.Room
import com.example.alphastick.data.local.AppDatabase
import com.example.alphastick.data.local.PermissionLogDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(app: Application): AppDatabase {
        return Room.databaseBuilder(
            app,
            AppDatabase::class.java,
            "alphastick.db"
        ).build()
    }

    @Provides
    @Singleton
    fun providePermissionLogDao(db: AppDatabase): PermissionLogDao {
        return db.permissionLogDao()
    }
}
