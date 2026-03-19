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

    suspend fun add(host: Host): Result<Void?> =
        withContext(ioDispatcher) {
            try {
                val res = service.add(host)
                if (res.isSuccessful) {
                    Result.success(null)
                } else {
                    Result.failure(Exception(res.errorBody()?.string()))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun delete(host: Host): Result<Void?> =
        withContext(ioDispatcher) {
            try {
                val res = service.delete(host.id!!)
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
