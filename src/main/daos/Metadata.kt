package main.daos

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import framework.models.BaseObject

/**
 * Metadata will be a key-value store
 *
 * @property id
 * @property key
 * @property value
 */

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
data class Metadatas(
    val metadatas: Map<String, String> = mutableMapOf()
): BaseObject