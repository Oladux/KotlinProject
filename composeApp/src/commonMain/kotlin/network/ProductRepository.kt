package org.example.project.network

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.plugins.*
import kotlinx.coroutines.CancellationException

class ProductRepository {

    suspend fun fetchProduct(): Result<ApiProduct> {
        return try {

            val product: ApiProduct =
                httpClient.get("https://fakestoreapi.com/products/1").body()

            Result.success(product)

        } catch (e: ClientRequestException) {
            Result.failure(Exception("Ошибка запроса (4xx)"))
        } catch (e: ServerResponseException) {
            Result.failure(Exception("Ошибка сервера (5xx)"))
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(Exception("Нет интернета или ошибка сети"))
        }
    }
}