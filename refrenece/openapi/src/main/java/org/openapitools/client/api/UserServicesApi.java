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
public class UserServicesApi {
    private ApiClient apiClient;

    public UserServicesApi() {
        this(new ApiClient());
    }

    public UserServicesApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * Delete a user
     *
     * <p><b>200</b> - User successfully deleted
     *
     * @param id The id parameter
     * @return Boolean
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec deleteUserRequestCreation(@jakarta.annotation.Nonnull Integer id) throws RestClientResponseException {
        Object postBody = null;
        // verify the required parameter 'id' is set
        if (id == null) {
            throw new RestClientResponseException("Missing the required parameter 'id' when calling deleteUser", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<>();

        pathParams.put("id", id);

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

        ParameterizedTypeReference<Boolean> localVarReturnType = new ParameterizedTypeReference<>() {
        };
        return apiClient.invokeAPI("/api/v1/users/{id}", HttpMethod.DELETE, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Delete a user
     *
     * <p><b>200</b> - User successfully deleted
     *
     * @param id The id parameter
     * @return Boolean
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    public Boolean deleteUser(@jakarta.annotation.Nonnull Integer id) throws RestClientResponseException {
        ParameterizedTypeReference<Boolean> localVarReturnType = new ParameterizedTypeReference<>() {
        };
        return deleteUserRequestCreation(id).body(localVarReturnType);
    }

    /**
     * Delete a user
     *
     * <p><b>200</b> - User successfully deleted
     *
     * @param id The id parameter
     * @return ResponseEntity&lt;Boolean&gt;
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<Boolean> deleteUserWithHttpInfo(@jakarta.annotation.Nonnull Integer id) throws RestClientResponseException {
        ParameterizedTypeReference<Boolean> localVarReturnType = new ParameterizedTypeReference<>() {
        };
        return deleteUserRequestCreation(id).toEntity(localVarReturnType);
    }

    /**
     * Delete a user
     *
     * <p><b>200</b> - User successfully deleted
     *
     * @param id The id parameter
     * @return ResponseSpec
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec deleteUserWithResponseSpec(@jakarta.annotation.Nonnull Integer id) throws RestClientResponseException {
        return deleteUserRequestCreation(id);
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

    /**
     * Register a new user
     *
     * <p><b>201</b> - User successfully created
     *
     * @param name The name parameter
     * @return User
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec registerRequestCreation(@jakarta.annotation.Nullable String name) throws RestClientResponseException {
        Object postBody = null;
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<>();

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "name", name));

        final String[] localVarAccepts = {
                "application/json"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[]{};

        ParameterizedTypeReference<User> localVarReturnType = new ParameterizedTypeReference<>() {
        };
        return apiClient.invokeAPI("/api/v1/users", HttpMethod.POST, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Register a new user
     *
     * <p><b>201</b> - User successfully created
     *
     * @param name The name parameter
     * @return User
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    public User register(@jakarta.annotation.Nullable String name) throws RestClientResponseException {
        ParameterizedTypeReference<User> localVarReturnType = new ParameterizedTypeReference<>() {
        };
        return registerRequestCreation(name).body(localVarReturnType);
    }

    /**
     * Register a new user
     *
     * <p><b>201</b> - User successfully created
     *
     * @param name The name parameter
     * @return ResponseEntity&lt;User&gt;
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<User> registerWithHttpInfo(@jakarta.annotation.Nullable String name) throws RestClientResponseException {
        ParameterizedTypeReference<User> localVarReturnType = new ParameterizedTypeReference<>() {
        };
        return registerRequestCreation(name).toEntity(localVarReturnType);
    }

    /**
     * Register a new user
     *
     * <p><b>201</b> - User successfully created
     *
     * @param name The name parameter
     * @return ResponseSpec
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec registerWithResponseSpec(@jakarta.annotation.Nullable String name) throws RestClientResponseException {
        return registerRequestCreation(name);
    }

    /**
     * Update a user&#39;s name
     *
     * <p><b>200</b> - User successfully updated
     *
     * @param id      The id parameter
     * @param newName The newName parameter
     * @return User
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    private ResponseSpec updateUserRequestCreation(@jakarta.annotation.Nonnull Integer id, @jakarta.annotation.Nonnull String newName) throws RestClientResponseException {
        Object postBody = null;
        // verify the required parameter 'id' is set
        if (id == null) {
            throw new RestClientResponseException("Missing the required parameter 'id' when calling updateUser", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // verify the required parameter 'newName' is set
        if (newName == null) {
            throw new RestClientResponseException("Missing the required parameter 'newName' when calling updateUser", HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
        }
        // create path and map variables
        final Map<String, Object> pathParams = new HashMap<>();

        pathParams.put("id", id);

        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, String> cookieParams = new LinkedMultiValueMap<>();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<>();

        queryParams.putAll(apiClient.parameterToMultiValueMap(null, "newName", newName));

        final String[] localVarAccepts = {
                "application/json"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        String[] localVarAuthNames = new String[]{};

        ParameterizedTypeReference<User> localVarReturnType = new ParameterizedTypeReference<>() {
        };
        return apiClient.invokeAPI("/api/v1/users/{id}", HttpMethod.PUT, pathParams, queryParams, postBody, headerParams, cookieParams, formParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * Update a user&#39;s name
     *
     * <p><b>200</b> - User successfully updated
     *
     * @param id      The id parameter
     * @param newName The newName parameter
     * @return User
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    public User updateUser(@jakarta.annotation.Nonnull Integer id, @jakarta.annotation.Nonnull String newName) throws RestClientResponseException {
        ParameterizedTypeReference<User> localVarReturnType = new ParameterizedTypeReference<>() {
        };
        return updateUserRequestCreation(id, newName).body(localVarReturnType);
    }

    /**
     * Update a user&#39;s name
     *
     * <p><b>200</b> - User successfully updated
     *
     * @param id      The id parameter
     * @param newName The newName parameter
     * @return ResponseEntity&lt;User&gt;
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseEntity<User> updateUserWithHttpInfo(@jakarta.annotation.Nonnull Integer id, @jakarta.annotation.Nonnull String newName) throws RestClientResponseException {
        ParameterizedTypeReference<User> localVarReturnType = new ParameterizedTypeReference<>() {
        };
        return updateUserRequestCreation(id, newName).toEntity(localVarReturnType);
    }

    /**
     * Update a user&#39;s name
     *
     * <p><b>200</b> - User successfully updated
     *
     * @param id      The id parameter
     * @param newName The newName parameter
     * @return ResponseSpec
     * @throws RestClientResponseException if an error occurs while attempting to invoke the API
     */
    public ResponseSpec updateUserWithResponseSpec(@jakarta.annotation.Nonnull Integer id, @jakarta.annotation.Nonnull String newName) throws RestClientResponseException {
        return updateUserRequestCreation(id, newName);
    }
}
