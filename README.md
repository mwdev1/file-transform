# File Transform Application

## Overview

This project is a Spring Boot application designed to process a file and transform it to an output file returned to the client.
This can be done by sending the initial file using the provided REST API endpoint.

The application also includes IP filtering, file validation, and other features as outlined below. 

#### IP Address Whitelisting

Before processing the file, the application validates that the request comes from a valid IP address using the API from ip-api.com.

http://ip-api.com/json/{query}

#### Logging

For every file processing request, the following details will be logged in H2 database.

### API Endpoints

**Process File:**

    URL: http://localhost:8080/api/v1/files/process
    Method: POST
    Request: multipart/form-data containing "text/plain" file.
    Response: JSON data attached as "OutcomeFile.json" file.
    
    Error scenarios:
    400 - Bad request
    403 - Access denied

curl sample:

    curl --location 'http://localhost:8080/api/v1/files/process' \
    --form 'file=@"/C:/repos/interviews/uploader/input.txt"'


**Health Check:**

    URL: http://localhost:8080/actuator/health
    Method: GET
    Response: 200 OK if the application is running.

## Configuration

Application specific configuration can be found in the application.yml file under section "application":

```yaml
application:
  clients:
    ip-validation:
      url: 'http://ip-api.com/json/%s'
  security:
    blocked-countries: China,Spain,USA
    blocked-isps: Amazon,Google Cloud,Microsoft Azure
  service:
    validate-content: true
    input-attributes: UUID,ID,Name,Likes,Transport,Avg Speed,Top Speed
    output-attributes: Name, Transport, Top Speed
    record-validation-types: UUID,IDENTIFIER,NAME,NAME,NAME,POSITIVE_NUMBER,POSITIVE_NUMBER
```
The different sections above control the application behaviour:
#### Security controls the API access.
#### Under the service section we have:
- **input-attributes** - the property defines the expected data input structure. 
The input file must conform to this property definition. 
- If at least one of the input file data has a different line structure (number of fields) then the file is rejected.
- **output-attributes** - the property defines the expected data output structure
- **validation-enabled** - a feature flag to turn on/off validation feature. If turned on, 
**record-validation-types** must be configured as well.
- **record-validation-types** to indicate what kind of validation rules should be applied to all individual fields.
If no validation should be applicable, 'NONE' value should be set for the corresponding field.
**record-validation-types** must match all individual fields configured in **input-attributes** property.

##### Validation types:
- IDENTIFIER: Only capital letters and numbers allowed
- NAME: Special characters are not allowed.
- NONE: Not validated
- POSITIVE_NUMBER: Must be greater than zero
- UUID: Must be valid UUID format
