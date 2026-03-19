package edu.oregonstate.joneset3.iplist.data

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class HostDatabaseRepo(
    private val service: HostDatabaseService,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend fun loadHosts(): Result<List<Host>> =
        withContext(ioDispatcher) {
            try {
                val res = service.getAll()
                if (res.isSuccessful) {
                    Result.success(res.body() ?: listOf())
                } else {
                    Result.failure(Exception(res.errorBody()?.string()))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun addHost(host: Host): Result<Void?> =
        withContext(ioDispatcher) {
            try {
                val res = service.addHost(host)
                if (res.isSuccessful) {
                    Result.success(null)
                } else {
                    Result.failure(Exception(res.errorBody()?.string()))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}
