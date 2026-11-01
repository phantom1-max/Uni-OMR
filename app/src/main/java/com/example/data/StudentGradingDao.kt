package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.model.StudentGradingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentGradingDao {
    @Query("SELECT * FROM student_gradings WHERE examId = :examId ORDER BY totalScore DESC, createdAt DESC")
    fun getGradingsForExam(examId: String): Flow<List<StudentGradingEntity>>

    @Query("SELECT * FROM student_gradings WHERE id = :id LIMIT 1")
    suspend fun getGradingById(id: String): StudentGradingEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGrading(grading: StudentGradingEntity)

    @Query("DELETE FROM student_gradings WHERE id = :id")
    suspend fun deleteGradingById(id: String)

    @Query("DELETE FROM student_gradings WHERE examId = :examId")
    suspend fun deleteGradingsForExam(examId: String)

    @Query("SELECT COUNT(*) FROM student_gradings WHERE examId = :examId")
    fun getGradingCountForExam(examId: String): Flow<Int>
}
