How to setup and test the functionality:

1.	Clone the repository: git clone 
2.	Ensure Docker is running on the host.
3.	Build the Docker images: docker-compose build --no-cache
4.	Start the services: docker-compose up but go to the docker-compose.yml and give the volume path (Yourhost folder: container volume) in my case it was defined as
Keep the containers mount name as content as the code goes there , also it could be configured through a prop file which was better.

 volumes:
      - C:/dbtune-drive:/content


6.	Verify all services are running: docker ps
 
7.	Open your browser and navigate to http://localhost:3000/submit to access the file upload option.
8.	Upload a file and confirm the upload message indicates the file status is "STORED."
 
9.	Check the file status using the API: GET http://localhost:8080/cms/list?page=0&size=10. The files status is STORED 
 
10.	Process the file for resizing and thumbnail creation by issuing:
    
 - 	GET http://localhost:8080/resize/images
 -	GET http://localhost:8080/resize/thumbnails
   
10.	Verify the file status and details using the API: GET http://localhost:8080/cms/list?page=0&size=10. The status should be moved from  "STORED," with updated file and thumbnail locations.
 
  
11.	Now try to visit http://localhost:3000/list , you will have paginated GRID and TABLE VIEW.

11.1 GRID allows you to download the resized image (paginated set) with other image attributes like name 

11.2 TABLE allows you to download the thumbnails of the paginated set with other attributes 

11.2.1 Once you click on the thumbnail the server issues the complete resized file .




Table view details:
 

Expanded view of the image form the Table view ,it has a close button to move back to the table .
 

GRID view displays the full image by default ( though the fetching of the images is paginated ) .
Also you can see the content from the mounted volume just as a test, the directory sits behind the fileserver service but is never visible to the user.


The application is built using React for the frontend, Spring Boot for the backend, and PostgreSQL as the database, with all components containerized using Docker. To enhance resiliency, I have ensured that the Content Receiver service operates independently of the File Resizer service which is an additional change i made as it seemed better.
 

=============================================================================================================================================================================


Main motivation to move this code to a cloud storage but it is not convered in this poc:

We're using a temporary folder for file uploads to manage storage efficiently. To avoid having too many files in a single directory, we should organize files into separate folders. Ideally, each user would have their own dedicated folder for their uploads.

For better scalability, we could consider using separate NFS (Network File System) servers for storing these files. This approach is similar to how cloud storage solutions like S3 handle file storage, allowing us to reference files by their unique IDs.

However, if we're managing this ourselves, it's important to be aware of filesystem limitations. For example:

Windows File System on the host (but the container has a Linux file system) has a theoretical limit of around 32,767 folders per directory. In practice, performance and the maximum path length may impose lower limits.
FAT32 (File Allocation Table 32) has a limit of 65,534 files per directory and a maximum path length of 260 characters.
As the number of users and uploads increases, it's crucial to manage file storage carefully to avoid hitting these filesystem limits and ensure optimal performance.

 
