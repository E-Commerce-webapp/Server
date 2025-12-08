package com.example.EcomSphere

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.retry.annotation.EnableRetry

@SpringBootApplication
@EnableRetry
class EcomSphereApplication

fun main(args: Array<String>) {
	runApplication<EcomSphereApplication>(*args)
}
