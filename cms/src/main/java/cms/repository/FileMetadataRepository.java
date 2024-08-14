package cms.repository;



import cms.model.FileMetadata;
import cms.model.FileStatus;
import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.UUID;

@Repository
public interface FileMetadataRepository extends JpaRepository<FileMetadata, UUID> {

    List<FileMetadata> findByStatus(FileStatus status);

    List<FileMetadata> findByIsThumbnailCreatedFalse();

    @Query("SELECT f FROM FileMetadata f WHERE f.id = :id AND f.status = :status")
    FileMetadata findByIdAndStatusWithLock(@Param("id") UUID id, @Param("status") FileStatus status);

    @Query("SELECT f FROM FileMetadata f WHERE f.userId = :userId")
    List<FileMetadata> findByUserId(@Param("userId") UUID userId);

    Page<FileMetadata> findByUserIdAndFileTypeIn(UUID userId, List<String> fileTypes, Pageable pageable);

    List<FileMetadata> findByUserIdAndFileTypeIn(UUID userId, List<String> imageFileTypes);


    @Modifying
    @Transactional
    @Query("UPDATE FileMetadata f SET f.status = :newStatus WHERE f.id = :id AND f.status = :oldStatus")
    int updateStatus(@Param("id") UUID id, @Param("oldStatus") FileStatus oldStatus, @Param("newStatus") FileStatus newStatus);

    // New method to find files by status and file type
    List<FileMetadata> findByStatusAndFileTypeStartingWith(FileStatus status, String fileTypePrefix);



}
