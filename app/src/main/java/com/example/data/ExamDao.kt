package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.model.ExamEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExamDao {
    @Query("SELECT * FROM exams ORDER BY createdAt DESC")
    fun getAllExams(): Flow<List<ExamEntity>>

    @Query("SELECT * FROM exams WHERE id = :id LIMIT 1")
    suspend fun getExamById(id: String): ExamEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExam(exam: ExamEntity)

    @Update
    suspend fun updateExam(exam: ExamEntity)

    @Query("DELETE FROM exams WHERE id = :id")
    suspend fun deleteExamById(id: String)
}
