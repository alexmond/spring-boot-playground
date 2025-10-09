# UserServicesApi

All URIs are relative to *http://localhost*

| Method                                            | HTTP request                  | Description              |
|---------------------------------------------------|-------------------------------|--------------------------|
| [**deleteUser**](UserServicesApi.md#deleteUser)   | **DELETE** /api/v1/users/{id} | Delete a user            |
| [**getAllUsers**](UserServicesApi.md#getAllUsers) | **GET** /api/v1/users         | Returns all users        |
| [**register**](UserServicesApi.md#register)       | **POST** /api/v1/users        | Register a new user      |
| [**updateUser**](UserServicesApi.md#updateUser)   | **PUT** /api/v1/users/{id}    | Update a user&#39;s name |

## deleteUser

> Boolean deleteUser(id)

Delete a user

### Example

```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.models.*;
import org.openapitools.client.api.UserServicesApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost");

        UserServicesApi apiInstance = new UserServicesApi(defaultClient);
        Integer id = 56; // Integer | 
        try {
            Boolean result = apiInstance.deleteUser(id);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling UserServicesApi#deleteUser");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            e.printStackTrace();
        }
    }
}
```

### Parameters

| Name   | Type        | Description | Notes |
|--------|-------------|-------------|-------|
| **id** | **Integer** |             |       |

### Return type

**Boolean**

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json

### HTTP response details

| Status code | Description               | Response headers |
|-------------|---------------------------|------------------|
| **200**     | User successfully deleted | -                |

## getAllUsers

> User getAllUsers()

Returns all users

### Example

```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.models.*;
import org.openapitools.client.api.UserServicesApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost");

        UserServicesApi apiInstance = new UserServicesApi(defaultClient);
        try {
            User result = apiInstance.getAllUsers();
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling UserServicesApi#getAllUsers");
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

[**User**](User.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json

### HTTP response details

| Status code | Description       | Response headers |
|-------------|-------------------|------------------|
| **200**     | Returns all users | -                |

## register

> User register(name)

Register a new user

### Example

```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.models.*;
import org.openapitools.client.api.UserServicesApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost");

        UserServicesApi apiInstance = new UserServicesApi(defaultClient);
        String name = "Stranger"; // String | 
        try {
            User result = apiInstance.register(name);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling UserServicesApi#register");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            e.printStackTrace();
        }
    }
}
```

### Parameters

| Name     | Type       | Description | Notes                            |
|----------|------------|-------------|----------------------------------|
| **name** | **String** |             | [optional] [default to Stranger] |

### Return type

[**User**](User.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json

### HTTP response details

| Status code | Description               | Response headers |
|-------------|---------------------------|------------------|
| **201**     | User successfully created | -                |

## updateUser

> User updateUser(id, newName)

Update a user&#39;s name

### Example

```java
// Import classes:
import org.openapitools.client.ApiClient;
import org.openapitools.client.ApiException;
import org.openapitools.client.Configuration;
import org.openapitools.client.models.*;
import org.openapitools.client.api.UserServicesApi;

public class Example {
    public static void main(String[] args) {
        ApiClient defaultClient = Configuration.getDefaultApiClient();
        defaultClient.setBasePath("http://localhost");

        UserServicesApi apiInstance = new UserServicesApi(defaultClient);
        Integer id = 56; // Integer | 
        String newName = "newName_example"; // String | 
        try {
            User result = apiInstance.updateUser(id, newName);
            System.out.println(result);
        } catch (ApiException e) {
            System.err.println("Exception when calling UserServicesApi#updateUser");
            System.err.println("Status code: " + e.getCode());
            System.err.println("Reason: " + e.getResponseBody());
            System.err.println("Response headers: " + e.getResponseHeaders());
            e.printStackTrace();
        }
    }
}
```

### Parameters

| Name        | Type        | Description | Notes |
|-------------|-------------|-------------|-------|
| **id**      | **Integer** |             |       |
| **newName** | **String**  |             |       |

### Return type

[**User**](User.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: application/json

### HTTP response details

| Status code | Description               | Response headers |
|-------------|---------------------------|------------------|
| **200**     | User successfully updated | -                |

