# Image Object Detection Service
This is a small project intended to produce a server capable of providing services related to detecting objects in images. 

## Requirements
The dependencies and version used are as follows:
 - Java: [21.0.3](https://adoptium.net/temurin/releases/)
 - Maven: [3.9.8](https://maven.apache.org/download.cgi)
 - PostgreSQL: [16.3](https://www.enterprisedb.com/downloads/postgres-postgresql-downloads)

## Setup
### PostgreSQL Database
A database is required for basic operations like storing/retrieving image metadata and searching for images containing objects. PostgreSQL would be easiest to use since `sql/get_object_id.sql` is specifically written for it, but it wouldn't be too much trouble to rewrite it for another RDBMS.

After creating a database, executing `sql/images_objects_tags_tables.sql` creates the required tables and relationships, and executing `sql/get_object_id.sql` creates the one stored function.

### SpringBoot API Server
Some properties will need to be modified to run the server. 
 - In `src/main/resources/application.properties`, the datasource properties should be updated to match the DB from the previous step. 
 - The `imagga.basic.auth` key should come from https://imagga.com/, which allows free signups with a limit of 1000 calls per month.
    - To use a different object detection API, the `com.xoab.imageObjectDetection.helper.ImageObjectDetector` interface should be implemented

Once these modifications have been made, in the root of this project, run the following commands. This should download all java dependencies, run tests, build an executable jar, and start the application.
```cmd
mvn install
java -jar ImageObjectDetection-0.0.1-SNAPSHOT.jar
```

## Endpoints
To call the APIs, the swagger page can be used and should be available at http://localhost:8080/swagger-ui/index.html. Additionally, `src/main/resources/test-requests.http` shows example calls that can be made (and used in IntelliJ IDEA). The following endpoints are available:

### Upload an image
```
POST /images
```
Uploads an image to the server.

#### Request Body
|Key | Type | Value |
|-|-|-|
|**imageData**|byte array | an image file (optional; if empty, must have imageUrl)|
|**imageUrl**|string |URL of publicly available image (optional; if empty, must have imageData)|
|**imageLabel**| string| (optional) label for the image|
|**enableObjectDetection**| boolean|(optional) of whether to detect objects|

#### Response Body
|Key | Type | Value |
|-|-|-|
|**id**|integer| value of the image's id in the database|
|**label**| string|(optional| a label for the image (produced by server if not provided in request)|
|**imageData**|byte array | the image file data|
|**url**|string| URL of the file used by the server|
|**objectDetectionEnabled**| boolean| whether object detection was enabled|
|**objects**| string|(optional)list of objects identified in the image if object detection was enabled|

### Get an image
```
GET /images/{imageId}
```
Retrieves the data associated with the image of id `imageId` if it exists in the database.
#### Response Body
|Key | Type | Value |
|-|-|-|
|**id**|integer| value of the image's id in the database|
|**label**| string|(optional| a label for the image (produced by server if not provided in request)|
|**imageData**|byte array | the image file data|
|**url**|string| URL of the file used by the server|
|**objectDetectionEnabled**| boolean| whether object detection was enabled|
|**objects**| string|(optional)list of objects identified in the image if object detection was enabled|

### Get all images
```
GET /images
```
Retrieves the metadata of all images stored in the database.

#### Response Body
The response is a list, with each item containing the following valid attributes:
|Key | Type | Value |
|-|-|-|
|**id**|integer| value of the image's id in the database|
|**label**| string|(optional| a label for the image (produced by server if not provided in request)|
|**url**|string| URL of the file used by the server|
|**objectDetectionEnabled**| boolean| whether object detection was enabled|
|**objects**| string|(optional)list of objects identified in the image if object detection was enabled|


### Get images with objects
```
GET /images?objects={cat,dog}
```
Retrieves the metadata of all images with the requested objects.

#### Response Body
The response is a list, with each item containing the following valid attributes:
|Key | Type | Value |
|-|-|-|
|**id**|integer| value of the image's id in the database|
|**url**|string| URL of the file used by the server|

## Tests
There are some tests under `src/test/java` that will be run by maven on install; these should help confirm database connections are working as well as the endpoints. Of course, more thorough testing should be added.
