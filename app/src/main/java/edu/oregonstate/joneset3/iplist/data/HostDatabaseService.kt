package edu.oregonstate.joneset3.iplist.data

import com.squareup.moshi.Moshi
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

/**
 * This is a Retrofit service interface encapsulating communication with the Host Database.
 */
interface HostDatabaseService {
    /**
     * Fetches all hosts from the database.
     *
     * @return Returns a Retrofit `Response<>` object that will contain a [List<Host>] object
     *   if the API call was successful.
     */
    @GET("getall")
    suspend fun getAll() : Response<List<Host>>

    /**
     * Posts a new host to the database.
     *
     * @param host The host object to be added to the database.
     * @return Returns a Retrofit `Response<>` object.
     */
    @POST("add")
    suspend fun addHost(@Body host: Host) : Response<Void>

    companion object {
        private const val BASE_URL = "http://172.16.0.10:5000"

        /**
         * This method can be called as `HostDatabaseService.create()` to create an object
         * implementing the HostDatabaseService interface and which can be used to make calls to
         * the database.
         */
        fun create() : HostDatabaseService {
            val moshi = Moshi.Builder()
                .add(IPv4Adapter())
                .build()
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(HostDatabaseService::class.java)
        }
    }
}
