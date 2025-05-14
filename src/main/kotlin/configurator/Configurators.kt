package io.tujh.configurator

import io.ktor.server.application.*
import io.tujh.auth.AuthConfigurator
import io.tujh.posts.PostsConfigurator
import io.tujh.profile.ProfileConfigurator
import io.tujh.serialization.SerializationConfigurator

val configurators = listOf<(Application) -> Configurator>(
    ::AuthConfigurator,
    ::SerializationConfigurator,
    ::PostsConfigurator,
    ::ProfileConfigurator
)