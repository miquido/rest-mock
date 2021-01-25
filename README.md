# RestMock

Library that provides mocked responses for REST API calls by using [OkHttp MockWebServer](https://github.com/square/okhttp/tree/master/mockwebserver).

RestMock library can be useful, for example, for Android developers to build apps that use OkHttpClient allowing them to mock some API responses. It can be helpful when you work with endpoints that are not available yet, so you can mock them, while other API calls will call the original server. Or you want to test different scenarios with responses or error responses. Or you can call mocked API in unit tests.


## I. Setup.

```
repositories {
    jcenter()
}
```
Add RestMock dependency:
`implementation 'com.miquido:restmock:1.0.0'`

The best way to separate app build with mocked api is to create a flavor for it. Let's name it "devMock":

```
flavorDimensions "environment"

productFlavors {
    dev {
        applicationIdSuffix ".dev"
        versionName versionName + "-DEV"
        manifestPlaceholders = [appName: "RestMockSample-DEV"]
    }

    devMock {
        applicationIdSuffix ".dev.mock"
        versionName versionName + "-DEV-MOCK"
        manifestPlaceholders = [appName: "RestMockSample-DEV-MOCK"]
    }

    prod {
        manifestPlaceholders = [appName: "RestMockSample"]
    }
}
```

Don't forget to exclude mock json files from all builds except a build with mocked API.
You can do it simply by adding mock json files to a particular flavor (devMock in our case) source folder or just exclude files this way:
```
// exclude mocks for all flavors except devMock
if (!getGradle().getStartParameter().getTaskNames().toString().contains("DevMock")){
    packagingOptions {
        exclude '/mocks/**'
    }
}
```

## II. Mock API responses.

In general it's up to you where to keep jsons (API mock responses), but you can go the same way as in our sample application.

You would need to define a folder name for your mocks

```
private const val MOCKS_FOLDER = "mocks"
```

Add 2 simple methods (you can modify it for your own needs):
```
private fun getStringFromResource(fileName: String): String {
    val classLoader = RequestFilter::class.java.classLoader
    return classLoader?.getResourceAsStream("$MOCKS_FOLDER/$fileName")?.bufferedReader()
        ?.use { it.readText() }
        ?: throw IllegalArgumentException("Cannot find `$fileName` in resources")
}

private fun createMockResponse(code: Int, fileName: String?): MockResponse {
    return MockResponse().apply {
        setResponseCode(code)
        setHeader("Content-Type", "application/json")
        if (!fileName.isNullOrBlank()) {
            setBody(getStringFromResource(fileName))
        }
    }
}
```

Add your json mocks under `src/main/resources/mocks` folder. Please note, it must be exactly *resources*, not *res*.

Then create a map of request filters to responses ([RequestFilter, MockResponse] pairs):

```
val sampleApiMocks: Map<RequestFilter, MockResponse> = mapOf(

    RequestFilter(
        method = Method.GET,
        path = Path.Exact("/api/v1/cars")
    ) to createMockResponse(200, "get_cars_200.json"),

    RequestFilter(
        method = Method.GET,
        path = Path.Exact("/api/v1/cars"),
        query = mapOf("make" to Query.Exact("Ford"))
    ) to createMockResponse(200, "get_cars_make_ford_200.json"),

    ...
    ...
)
```

And now when you build an OkHttpClient instance set your mocks:

```
private var apiMocked: Boolean = true/false
...
...
OkHttpClient.Builder()
    .setMocks(apiMocked) {
        arrayOf(
            sampleApiMocks
            // add other api mocks here
        )
    }
    .build()
```
