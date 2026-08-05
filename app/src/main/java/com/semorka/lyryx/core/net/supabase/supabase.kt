package com.semorka.lyryx.core.net.supabase

import com.semorka.lyryx.BuildConfig
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.functions.Functions

val supabase = createSupabaseClient(
    supabaseUrl = BuildConfig.SUPABASE_URL,
    supabaseKey = BuildConfig.SUPABASE_KEY
) {
    install(Functions.Companion)
}