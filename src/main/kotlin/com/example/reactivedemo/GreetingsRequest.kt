package com.example.reactivedemo

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

data class GreetingsRequest(
        val name: String
)