package main.services.healthchecks

import main.daos.Healthcheck

object CheckDatabaseHealthService {
    fun execute() : Healthcheck {
        return try {
            Healthcheck("HEALTHY", "Successfully connected to database")
        } catch(e: Exception) {
            System.err.println("Failed to connect to database: $e.message")
            Healthcheck("UNHEALTHY", "Failed to connect to database: " + e.message)
        }
    }
}