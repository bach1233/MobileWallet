package vn.hvktmm.bach.securitylab.storage

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import vn.hvktmm.bach.securitylab.model.LabTransaction

class SensitiveDataManager(context: Context) : SQLiteOpenHelper(context, "mobiwallet_lab.db", null, 1) {
    override fun onCreate(db: SQLiteDatabase) { db.execSQL("CREATE TABLE transactions (id TEXT, sender TEXT, recipient TEXT, amount INTEGER, timestamp TEXT, status TEXT)") }
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
    @JvmOverloads fun saveTransaction(transaction: LabTransaction) {
        writableDatabase.execSQL("INSERT INTO transactions VALUES (?, ?, ?, ?, ?, ?)", arrayOf(transaction.id, transaction.sender, transaction.recipient, transaction.amount, transaction.timestamp, transaction.status))
    }
}
