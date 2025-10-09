# HelloWorldRestApi

All URIs are relative to *http://localhost*

| Method                                            | HTTP request   | Description |
|---------------------------------------------------|----------------|-------------|
| [**helloWorld**](HelloWorldRestApi.md#helloWorld) | **GET** /hello |             |

## helloWorld

> String helloWorld()

### Example

```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.models.*;
import org.openapitools.client.api.HelloWorldRestApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost");

        HelloWorldRestApi apiInstance = new HelloWorldRestApi(defaultClient);
        try {
            String result = apiInstance.helloWorld();
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling HelloWorldRestApi#helloWorld");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            e.printStackTrace();
        }
    }
}
```

### Parameters

This endpoint does not need any parameter.

### Return type

**String**

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: */*

### HTTP response details

| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200**     | OK          | -                |

