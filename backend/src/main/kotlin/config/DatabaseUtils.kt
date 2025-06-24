

import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.transactions.experimental.newSuspendedTransaction
import javax.sql.DataSource


//Configuración inicial (deberías hacer esto al iniciar tu aplicación)

object DatabaseFactory {
    fun init(dataSource: DataSource) {
        Database.connect(dataSource)
    }

    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}