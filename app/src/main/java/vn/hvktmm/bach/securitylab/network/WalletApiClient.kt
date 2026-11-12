package vn.hvktmm.bach.securitylab.network

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import vn.hvktmm.bach.securitylab.LabConfig
import vn.hvktmm.bach.securitylab.model.LabTransaction
import vn.hvktmm.bach.securitylab.model.WalletProfile

object WalletApiClient {
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()
    private val client: OkHttpClient by lazy { PinnedHttpClient.get() }

    fun login(username: String, password: String): Pair<String, WalletProfile> {
        val body = JSONObject().put("username", username).put("password", password).toString().toRequestBody(jsonMediaType)
        val response = execute(Request.Builder().url("${NetworkConfig.BASE_URL}/login").post(body).build())
        return response.getString("token") to profileFromJson(response.getJSONObject("profile"))
    }

    fun register(email: String, name: String, phone: String, password: String): Pair<String, WalletProfile> {
        val body = JSONObject()
            .put("email", email)
            .put("displayName", name)
            .put("phone", phone)
            .put("password", password)
            .toString().toRequestBody(jsonMediaType)
        val response = execute(Request.Builder().url("${NetworkConfig.BASE_URL}/register").post(body).build())
        return response.getString("token") to profileFromJson(response.getJSONObject("profile"))
    }

    fun balance(token: String): WalletProfile = profileFromJson(execute(authRequest("/wallet/balance", token).build()).getJSONObject("profile"))
    fun profile(token: String): WalletProfile = profileFromJson(execute(authRequest("/profile", token).build()).getJSONObject("profile"))
    fun transactions(token: String): List<LabTransaction> {
        val array: JSONArray = execute(authRequest("/transactions", token).build()).getJSONArray("transactions")
        return List(array.length()) { index -> transactionFromJson(array.getJSONObject(index)) }
    }
    fun transfer(token: String, recipient: String, amount: Long, pin: String): LabTransaction {
        val body = JSONObject().put("recipient", recipient).put("amount", amount).put("pin", pin).toString().toRequestBody(jsonMediaType)
        return transactionFromJson(execute(authRequest("/transfer", token).post(body).build()).getJSONObject("transaction"))
    }

    fun payment(token: String, type: String, recipient: String, amount: Long, pin: String = "2468", message: String = ""): LabTransaction {
        val body = JSONObject()
            .put("type", type)
            .put("recipient", recipient)
            .put("amount", amount)
            .put("pin", pin)
            .put("message", message)
            .toString().toRequestBody(jsonMediaType)
        return transactionFromJson(execute(authRequest("/payment", token).post(body).build()).getJSONObject("transaction"))
    }

    fun topUp(token: String, amount: Long, pin: String = "2468"): WalletProfile {
        val body = JSONObject().put("amount", amount).put("pin", pin)
            .toString().toRequestBody(jsonMediaType)
        return profileFromJson(execute(authRequest("/topup", token).post(body).build()).getJSONObject("profile"))
    }

    fun savings(token: String, amount: Long, deposit: Boolean, pin: String = "2468"): WalletProfile {
        val body = JSONObject().put("amount", amount).put("deposit", deposit).put("pin", pin)
            .toString().toRequestBody(jsonMediaType)
        return profileFromJson(execute(authRequest("/savings", token).post(body).build()).getJSONObject("profile"))
    }

    private fun authRequest(path: String, token: String): Request.Builder = Request.Builder()
        .url(NetworkConfig.BASE_URL + path)
        .header("Authorization", "Bearer $token")
        .header("X-Lab-Mode", if (LabConfig.VULNERABLE_MODE) "vulnerable" else "hardened")
    private fun execute(request: Request): JSONObject = client.newCall(request).execute().use { response ->
        val payload = response.body?.string().orEmpty()
        if (!response.isSuccessful) throw IllegalStateException("HTTP ${response.code}: $payload")
        JSONObject(payload)
    }
    private fun profileFromJson(value: JSONObject) = WalletProfile(
        value.getString("username"),
        nullableString(value, "email"),
        value.getString("displayName"),
        value.getLong("balance"),
        value.getBoolean("premium"),
        value.getBoolean("admin"),
        nullableString(value, "phone"),
        value.optLong("savingsBalance", 0L)
    )
    private fun transactionFromJson(value: JSONObject) = LabTransaction(
        value.getString("id"), value.getString("sender"), value.getString("recipient"),
        value.getLong("amount"), value.getString("timestamp"), value.getString("status"),
        value.optString("type", "Giao dịch"), nullableString(value, "message")
    )

    private fun nullableString(value: JSONObject, key: String): String? =
        if (!value.has(key) || value.isNull(key)) null else value.getString(key)
}
