# Project Setup and Testing Guide

## Setup Instructions

1. **Clone the Repository**

   ```bash
   git clone <repository-url>
   ```

2. **Ensure Docker is Running**

   Make sure Docker is up and running on your host machine.

3. **Build Docker Images**

   ```bash
   docker-compose build --no-cache
   ```

4. **Start the Services**

   Before starting, edit `docker-compose.yml` to specify the volume path:
   
   ```yaml
   volumes:
     - YOUR HOST FILE SYSTEM DIR :/content
   ```

   Then start the services:

   ```bash
   docker-compose up
   ```

5. **Verify Services Are Running**

   Check if all services are up and running:

   ```bash
   docker ps
   ```

6. **Access the Application**

   Open your browser and navigate to [http://localhost:3000/submit](http://localhost:3000/submit) to access the file upload option.

7. **Upload a File**

   Upload a file and confirm the upload message indicates the file status is `STORED`.

8. **Check File Status**

   Use the API to verify the file status:

   ```bash
   GET http://localhost:8080/cms/list?page=0&size=10
   ```

   Ensure the file status is `STORED`.

9. **Process File**

   To process the file for resizing and thumbnail creation, issue the following requests:

   ```bash
   GET http://localhost:8080/resize/images
   GET http://localhost:8080/resize/thumbnails
   ```

10. **Verify Updated File Status**

    Check the file status and details using:

    ```bash
    GET http://localhost:8080/cms/list?page=0&size=10
    ```

    The status should be updated from `STORED` with updated file and thumbnail locations.

11. **View Files**

    Visit [http://localhost:3000/list](http://localhost:3000/list) to see the paginated GRID and TABLE views:

    - **GRID View**: Allows you to download the resized image (paginated set) with other image attributes like name.
    - **TABLE View**: Allows you to download the thumbnails of the paginated set with other attributes.

    - **TABLE View Details**:
      - Clicking on a thumbnail will issue the complete resized file from the server.
      - The expanded view of the image includes a close button to return to the table.

    - **GRID View**:
      - Displays the full image by default (paginated fetching).
      - You can see the content from the mounted volume, which is behind the fileserver service but not visible to the user.

## Application Overview

The application uses:

- **Frontend**: React
- **Backend**: Spring Boot
- **Database**: PostgreSQL
- **Containerization**: Docker

**Content Receiver** and **File Resizer** services operate independently to enhance resiliency.

---

## Future Enhancements

### Cloud Storage Migration

While this POC uses a temporary folder for file uploads, consider these enhancements for cloud storage:

- **Organize Files**: Create separate folders for each user to manage storage efficiently.
- **Scalability**: Use separate NFS servers or cloud storage solutions like S3 to handle file storage, referencing files by their unique IDs.

### Filesystem Limitations

Be aware of filesystem limitations:

- **Windows File System**: Theoretical limit of 32,767 folders per directory.
- **FAT32**: Limit of 65,534 files per directory and a maximum path length of 260 characters.

Properly manage file storage to avoid hitting these limits and ensure optimal performance.
