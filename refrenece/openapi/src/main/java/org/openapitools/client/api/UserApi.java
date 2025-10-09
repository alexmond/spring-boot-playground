package org.openapitools.client.api;

import org.openapitools.client.ApiClient;

import org.openapitools.client.model.User;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient.ResponseSpec;
import org.springframework.web.client.RestClientResponseException;

@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", date = "2025-07-16T18:04:03.850859-04:00[America/Toronto]", comments = "Generator version: 7.14.0")
public class UserApi {
    private ApiClient apiClient;

    public UserApi() {
        this(new ApiClient());
    }

    public UserApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * Returns all users
     *
     * <p><b>200</b> - Returns all users
     *
     * @return User
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec getAllUsersRequestCreation() throws RestClientResponseException {
        Object postBody = null;
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<>();

        final String[] localVarAccepts = {
                "application/json"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[]{};

        ParameterizedTypeReference<User> localVarReturnType = new ParameterizedTypeReference<>() {
        };
        return apiClient.invokeAPI("/api/v1/users", HttpMethod.GET, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Returns all users
     *
     * <p><b>200</b> - Returns all users
     *
     * @return User
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    public User getAllUsers() throws RestClientResponseException {
        ParameterizedTypeReference<User> localVarReturnType = new ParameterizedTypeReference<>() {
        };
        return getAllUsersRequestCreation().body(localVarReturnType);
    }

    /**
     * Returns all users
     *
     * <p><b>200</b> - Returns all users
     *
     * @return ResponseEntity&lt;User&gt;
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<User> getAllUsersWithHttpInfo() throws RestClientResponseException {
        ParameterizedTypeReference<User> localVarReturnType = new ParameterizedTypeReference<>() {
        };
        return getAllUsersRequestCreation().toEntity(localVarReturnType);
    }

    /**
     * Returns all users
     *
     * <p><b>200</b> - Returns all users
     *
     * @return ResponseSpec
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec getAllUsersWithResponseSpec() throws RestClientResponseException {
        return getAllUsersRequestCreation();
    }
}
