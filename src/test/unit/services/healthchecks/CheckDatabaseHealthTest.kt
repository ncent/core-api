package test.unit.services.healthchecks

import io.kotlintest.shouldBe
import io.kotlintest.specs.WordSpec
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.extension.ExtendWith
import main.services.healthchecks.CheckDatabaseHealthService

@ExtendWith(MockKExtension::class)
class CheckDatabaseHealthTest : WordSpec() {
    init {
        "calling execute on a Database Health Check Service" should {
            "return healthy if the database connection works" {
                val result = CheckDatabaseHealthService.execute()
                result.message shouldBe "Successfully connected to database"
            }
        }
    }
}
