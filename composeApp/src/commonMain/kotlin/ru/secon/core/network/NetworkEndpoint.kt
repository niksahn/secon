package ru.secon.core.network

import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import io.ktor.util.reflect.TypeInfo
import io.ktor.util.reflect.typeInfo
import io.ktor.client.request.HttpRequestBuilder as KtorHttpRequestBuilder

/** Класс позволяющий создавать сетевые запросы.  */
abstract class NetworkEndpointScope {
    /** Создает HTTP-запрос с методом GET. */
    inline fun <reified Response> get(
        urlString: String,
        block: KtorHttpRequestBuilder.() -> Unit = {},
    ): HttpEndpoint<Response> = HttpEndpoint(
        responseTypeInfo = typeInfo<Response>(),
        httpRequestBuilder = HttpRequestBuilder().apply {
            method = HttpMethod.Get
            url(urlString)
            block(this)
        },
    )

    /** Создает HTTP-запрос с методом GET. */
    inline fun <reified Response, reified Body> get(
        urlString: String,
        body: Body,
        contentType: ContentType = ContentType.Application.Json,
        block: KtorHttpRequestBuilder.() -> Unit = {},
    ): HttpEndpoint<Response> = get(urlString) {
        contentType(contentType)
        setBody(body)
        block(this)
    }

    /** Создает HTTP-запрос с методом POST. */
    inline fun <reified Response> post(
        urlString: String,
        block: KtorHttpRequestBuilder.() -> Unit = {},
    ): HttpEndpoint<Response> = HttpEndpoint(
        responseTypeInfo = typeInfo<Response>(),
        httpRequestBuilder = HttpRequestBuilder().apply {
            method = HttpMethod.Post
            url(urlString)
            block(this)
        },
    )

    /** Создает HTTP-запрос с методом POST. */
    inline fun <reified Response, reified Body> post(
        urlString: String,
        body: Body,
        contentType: ContentType = ContentType.Application.Json,
        block: KtorHttpRequestBuilder.() -> Unit = {},
    ): HttpEndpoint<Response> = post(urlString) {
        contentType(contentType)
        setBody(body)
        block(this)
    }

    /** Создает HTTP-запрос с методом PUT. */
    inline fun <reified Response> put(
        urlString: String,
        block: KtorHttpRequestBuilder.() -> Unit = {},
    ): HttpEndpoint<Response> = HttpEndpoint(
        responseTypeInfo = typeInfo<Response>(),
        httpRequestBuilder = HttpRequestBuilder().apply {
            method = HttpMethod.Put
            url(urlString)
            block(this)
        },
    )

    /** Создает HTTP-запрос с методом PUT. */
    inline fun <reified Response, reified Body> put(
        urlString: String,
        body: Body,
        contentType: ContentType = ContentType.Application.Json,
        block: KtorHttpRequestBuilder.() -> Unit = {},
    ): HttpEndpoint<Response> = put(urlString) {
        contentType(contentType)
        setBody(body)
        block(this)
    }

    /** Создает HTTP-запрос с методом DELETE. */
    inline fun <reified Response> delete(
        urlString: String,
        block: KtorHttpRequestBuilder.() -> Unit = {},
    ): HttpEndpoint<Response> = HttpEndpoint(
        responseTypeInfo = typeInfo<Response>(),
        httpRequestBuilder = HttpRequestBuilder().apply {
            method = HttpMethod.Delete
            url(urlString)
            block(this)
        },
    )

    /** Создает HTTP-запрос с методом DELETE. */
    inline fun <reified Response, reified Body> delete(
        urlString: String,
        body: Body,
        contentType: ContentType = ContentType.Application.Json,
        block: KtorHttpRequestBuilder.() -> Unit = {},
    ): HttpEndpoint<Response> = delete(urlString) {
        contentType(contentType)
        setBody(body)
        block(this)
    }

    /** Представляет информацию о HTTP запросе. */
    class HttpEndpoint<Response>(
        internal val responseTypeInfo: TypeInfo,
        internal val httpRequestBuilder: KtorHttpRequestBuilder,
    )
}
