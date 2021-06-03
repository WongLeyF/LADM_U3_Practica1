package mx.tecnm.tepic.ladm_u3_practica1

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class dB(context: Context?, name: String?, factory: SQLiteDatabase.CursorFactory?, version: Int) :
    SQLiteOpenHelper(context, name, factory, version) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE APARTADO (idApartado INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, nombreCliente VARCHAR(200), producto VARCHAR(200), precio FLOAT)")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        TODO("Not yet implemented")
    }
}