package main.daos

import framework.models.BaseObject

class Healthcheck(
	val status: String,
	val message: String
): BaseObject