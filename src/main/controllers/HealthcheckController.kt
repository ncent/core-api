package main.controllers

import kotlinserverless.framework.controllers.DefaultController
import kotlinserverless.framework.controllers.RestController
import main.daos.Healthcheck
import main.helpers.ControllerHelper.RequestData

class HealthcheckController: DefaultController<Healthcheck>(), RestController<Healthcheck> {
    val defaultHealthyHealthCheck = Healthcheck("HEALTHY", "default")

    override fun findOne(requestData: RequestData, identifier: String?): Healthcheck {
        return defaultHealthyHealthCheck
    }

    override fun health(requestData: RequestData): Healthcheck {
        return DatabaseHealthcheckController().health(requestData)
    }
}

