package mx.tecnm.tepic.ladm_u3_practica1

import android.content.ContentValues
import android.database.sqlite.SQLiteException
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    var dB = dB(this, "dbApartado", null, 1)
    var db = FirebaseFirestore.getInstance()
    var dataArray = ArrayList<String>()
    var idArray = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        insertSQL.setOnClickListener {
            insert()
        }
        syncFS.setOnClickListener {
            syncFirestore()
        }
        readFS.setOnClickListener {
            getFirestore()
        }
    }

    private fun insert() {
        if (requiredEditText()) return
        val trans = dB.writableDatabase
        try {
            val data = ContentValues()
            data.put("nombreCliente", editTextnombreCliente.text.toString())
            data.put("producto", editTextProducto.text.toString())
            data.put("precio", editTextPrecio.text.toString())

            val result = trans.insert("APARTADO", null, data)
            if (result == -1L) mensaje("ERROR", "No se pudo agregar el apartado")
            else Toast.makeText(this, "Se agrego a exitosamente", Toast.LENGTH_SHORT).show()
            clear()

        } catch (e: SQLiteException) {
            mensaje("ERROR", e.message!!)
        } finally {
            trans.close()
        }
    }

    private fun delete(id: String) {
        db.collection("APARTADO").document(id).delete()
            .addOnSuccessListener {
                Toast.makeText(
                    this,
                    "Eliminado exitosamente",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Algo salió mal", Toast.LENGTH_LONG).show()
            }
    }

    private fun syncFirestore() {
        val trans = dB.readableDatabase
        val transW = dB.writableDatabase
        try {
            val respuesta = trans.query("APARTADO", arrayOf("*"), null, null, null, null, null)
            if (respuesta.moveToFirst()) {
                do {
                    val data = hashMapOf(
                        "nombreCliente" to respuesta.getString(1),
                        "producto" to respuesta.getString(2),
                        "precio" to respuesta.getString(3)
                    )
                    db.collection("APARTADO").add(data as Any)
                        .addOnFailureListener { mensaje("ERROR", it.message!!) }
                    transW.delete(
                        "APARTADO", "idApartado=?",
                        arrayOf(respuesta.getString(0))
                    )
                } while (respuesta.moveToNext())
                mensaje("INFO", "Se agregaron exitosamente")
            } else {
                Toast.makeText(this, "No has agregado nada a la lista", Toast.LENGTH_LONG).show()
            }
        } catch (e: SQLiteException) {
            mensaje("ERROR ", e.message!!)
        } finally {
            trans.close()
        }
    }

    private fun getFirestore() {
        db.collection("APARTADO").addSnapshotListener { querySnapshot, e ->
            if (e != null) {
                mensaje("ERROR", e.message!!)
                return@addSnapshotListener
            }
            dataArray.clear()
            idArray.clear()
            for (doc in querySnapshot!!) {
                val cad = "nombreCliente: ${doc.getString("nombreCliente")}" +
                        "\nproducto: ${doc.getString("producto")}" +
                        "\nprecio: ${doc.getString("precio")}"
                dataArray.add(cad)
                idArray.add(doc.id)
            }
            if (dataArray.isEmpty()) dataArray.add("No se encontró ningún apartado")
            resultApartado.adapter =
                ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, dataArray)
            this.registerForContextMenu(resultApartado)
            resultApartado.setOnItemClickListener { _, _, i, _ ->
                dialogDelUpt(i)
            }
        }
    }

    private fun dialogDelUpt(index: Int) {
        val id = this.idArray[index]
        AlertDialog.Builder(this).setTitle("Atencion!")
            .setMessage("¿Que desea hacer con \n ${dataArray[index]}?")
            .setPositiveButton("Cancelar") { _, _ -> }
            .setNegativeButton("Eliminar") { _, _ -> delete(id) }
            .show()
    }

    private fun requiredEditText(): Boolean {
        if (editTextnombreCliente.text.toString().trim() == "") {
            editTextnombreCliente.error = "El nombre del cliente es requerido!"
            editTextnombreCliente.hint = "Ingresa el nombre del cliente"
            return true
        }
        if (editTextProducto.text.toString().trim() == "") {
            editTextProducto.error = "El producto es requerido!"
            editTextProducto.hint = "Ingresa el nombre del producto"
            return true

        }
        if (editTextPrecio.text.toString().trim() == "") {
            editTextPrecio.error = "El precio es requerido!"
            editTextPrecio.hint = "Ingresa el precio"
            return true
        }
        return false
    }

    private fun clear() {
        editTextnombreCliente.setText("")
        editTextProducto.setText("")
        editTextPrecio.setText("")
    }

    private fun mensaje(title: String, message: String) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Aceptar") { d, _ -> d.dismiss() }
            .show()
    }
}