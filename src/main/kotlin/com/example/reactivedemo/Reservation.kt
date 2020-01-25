package com.example.reactivedemo

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document
public data class Reservation(
        @Id
        val id: String?,
        val name: String
)