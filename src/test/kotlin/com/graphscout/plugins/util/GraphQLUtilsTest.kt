package com.graphscout.plugins.util

import com.graphscout.data.RequestMetaInfo
import com.graphscout.service.RequestVault
import org.json.JSONObject
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GraphQLUtilsTest {

    @Test
    fun `Testing the utility functions`() {
        val requestBody = """
            # var ccc=query {country(code: "$")(name)}
            query { allPlanets {planets{name population}}}
            """.trimMargin()
        assertEquals("allPlanets",GraphQLUtils.getFirstTypeName(GraphQLUtils.makeRequestOfficial(requestBody)))
    }
    @Test
    fun `other test`() {
        val r = """{"query":"query getOrszag{ country( code: \"AT\") { code name capital  currency }}"}""".trimIndent()
        println(GraphQLUtils.makeRequestOfficial(r))
    }


    @Test
    fun `result parser test`() {
        val resultData = """
{"data":{"conferences":[
{"locations":[{"name":"Amanda","aa":{"name":"Finland","ccc":"FI"}}]},
{"locations":[{"name":"Pörssitalo","aa":{"name":"Finland","ccc":"FI"}}]},
{"locations":[{"name":"SAE Vienna","aa":{"name":"Austria","ccc":"AT"}}]},
{"locations":[{"name":"Ankersaal","aa":{"name":"Austria","ccc":"AT"}}]},
{"locations":[{"name":"Wirtschafts Universität Wien","aa":{"name":"Austria","ccc":"AT"}}]},
{"locations":[{"name":"Elisa Appelsiini","aa":{"name":"Finland","ccc":"FI"}},{"name":"Valkoinen Sali","aa":{"name":"Finland","ccc":"FI"}}]},
{"locations":[{"name":"Paasitorni Congress Hall","aa":{"name":"Finland","ccc":"FI"}},{"name":"Tiivistämö","aa":{"name":"Finland","ccc":"FI"}}]},
{"locations":[]},{"locations":[]},
{"locations":[{"name":"Paasitorni Congress Hall","aa":{"name":"Finland","ccc":"FI"}},{"name":"Babylon Club & Garden","aa":{"name":"Finland","ccc":"FI"}}]},
{"locations":[]},{"locations":[{"name":"Paasitorni Congress Hall","aa":{"name":"Finland","ccc":"FI"}}]},
{"locations":[{"name":"Paasitorni Congress Hall","aa":{"name":"Finland","ccc":"FI"}}]}]}}
        """.trimIndent()
        assertTrue { GraphQLUtils.findKeyValues(JSONObject(resultData),"ccc").distinct().size == 2 }

        println( GraphQLUtils.findKeyValues(JSONObject(resultData),"ccc") )
        println( GraphQLUtils.findKeyValues(JSONObject(resultData),"aa") )
    }

    @Test
    fun `yet another test`() {
        val payload = JSONObject("""{"data":{"conferences":[{"locations":[{"country":{"vvv":"FI","name":"Finland"},"name":"Amanda"}],"id":"freezing-edge-2020"},{"locations":[{"country":{"vvv":"FI","name":"Finland"},"name":"Pörssitalo"}],"id":"future-frontend-2023"},{"locations":[{"country":{"vvv":"AT","name":"Austria"},"name":"SAE Vienna"}],"id":"techmovienight"},{"locations":[{"country":{"vvv":"AT","name":"Austria"},"name":"Ankersaal"}],"id":"halfstack-vienna-2019"},{"locations":[{"country":{"vvv":"AT","name":"Austria"},"name":"Wirtschafts Universität Wien"}],"id":"reason-conf-2019"},{"locations":[{"country":{"vvv":"FI","name":"Finland"},"name":"Elisa Appelsiini"},{"country":{"vvv":"FI","name":"Finland"},"name":"Valkoinen Sali"}],"id":"react-finland-2018"},{"locations":[{"country":{"vvv":"FI","name":"Finland"},"name":"Paasitorni Congress Hall"},{"country":{"vvv":"FI","name":"Finland"},"name":"Tiivistämö"}],"id":"react-finland-2019"},{"locations":[],"id":"react-finland-2020"},{"locations":[],"id":"react-finland-2021"},{"locations":[{"country":{"vvv":"FI","name":"Finland"},"name":"Paasitorni Congress Hall"},{"country":{"vvv":"FI","name":"Finland"},"name":"Babylon Club & Garden"}],"id":"react-finland-2022"},{"locations":[],"id":"typeof-2019"},{"locations":[{"country":{"vvv":"FI","name":"Finland"},"name":"Paasitorni Congress Hall"}],"id":"graphql-finland-2018"},{"locations":[{"country":{"vvv":"FI","name":"Finland"},"name":"Paasitorni Congress Hall"}],"id":"graphql-finland-2020"}]}}""")
        assertTrue { GraphQLUtils.findKeyValues(payload,"vvv").distinct().size == 2 }
    }

    @Test fun gergerg() {
        var qqq = """# this_is_from_a_different_api=query getTheThing {country(code: "${'$'}") { name capital continent {name} emoji currency awsRegion currencies languages { name native}}}
# qalias this_is_from_a_different_api={"query":"query getTheThing {country(code: \"${'$'}\") { emoji languages {name}}}"}
# alias this_is_from_a_different_api=https://restcountries.com/v3.1/alpha/${'$'}?fields=name,capital,flag
query Valami {
	conferences {
		id
		locations {
			name
			country {
				
				name
				this_is_from_a_different_api: code
			}
		}
	}
}
"""
        RequestVault.add(RequestMetaInfo((1)))
        val q2 = GraphQLUtils.makeRequestOfficial(qqq)
        GraphQLUtils.gatherAliasesFromRequestAndPutInTheRequestVault(1, q2)
    }
}
