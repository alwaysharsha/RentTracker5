package com.renttracker.app.data.dao

import androidx.room.*
import com.renttracker.app.data.model.Expense
import com.renttracker.app.data.model.ExpenseCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses ORDER BY date DESC")
    fun getAllExpenses(): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE id = :id")
    suspend fun getExpenseById(id: Long): Expense?

    @Query("SELECT * FROM expenses WHERE id = :id")
    fun getExpenseByIdFlow(id: Long): Flow<Expense?>

    @Query("SELECT * FROM expenses WHERE buildingId = :buildingId ORDER BY date DESC")
    fun getExpensesByBuilding(buildingId: Long): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE vendorId = :vendorId ORDER BY date DESC")
    fun getExpensesByVendor(vendorId: Long): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE category = :category ORDER BY date DESC")
    fun getExpensesByCategory(category: ExpenseCategory): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getExpensesByDateRange(startDate: Long, endDate: Long): Flow<List<Expense>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: Expense): Long

    @Update
    suspend fun updateExpense(expense: Expense)

    @Delete
    suspend fun deleteExpense(expense: Expense)
}
