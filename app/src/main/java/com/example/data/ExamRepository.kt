package com.example.data

import com.example.model.ExamEntity
import com.example.model.StudentGradingEntity
import kotlinx.coroutines.flow.Flow

class ExamRepository(
    private val examDao: ExamDao,
    private val studentGradingDao: StudentGradingDao
) {
    val allExams: Flow<List<ExamEntity>> = examDao.getAllExams()

    suspend fun getExamById(id: String): ExamEntity? = examDao.getExamById(id)

    suspend fun insertExam(exam: ExamEntity) = examDao.insertExam(exam)

    suspend fun updateExam(exam: ExamEntity) = examDao.updateExam(exam)

    suspend fun deleteExam(id: String) {
        studentGradingDao.deleteGradingsForExam(id)
        examDao.deleteExamById(id)
    }

    fun getGradingsForExam(examId: String): Flow<List<StudentGradingEntity>> =
        studentGradingDao.getGradingsForExam(examId)

    suspend fun getGradingById(id: String): StudentGradingEntity? =
        studentGradingDao.getGradingById(id)

    suspend fun insertGrading(grading: StudentGradingEntity) =
        studentGradingDao.insertGrading(grading)

    suspend fun deleteGrading(id: String) =
        studentGradingDao.deleteGradingById(id)

    fun getGradingCountForExam(examId: String): Flow<Int> =
        studentGradingDao.getGradingCountForExam(examId)
}
