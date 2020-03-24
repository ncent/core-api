package main.controllers

import kotlinserverless.framework.controllers.DefaultController
import kotlinserverless.framework.controllers.RestController
import main.daos.Healthcheck
import main.helpers.ControllerHelper.RequestData
import main.services.healthchecks.CheckDatabaseHealthService

class DatabaseHealthcheckController: DefaultController<Healthcheck>(), RestController<Healthcheck> {
    override fun health(requestData: RequestData): Healthcheck {
        return CheckDatabaseHealthService.execute()
    }
}