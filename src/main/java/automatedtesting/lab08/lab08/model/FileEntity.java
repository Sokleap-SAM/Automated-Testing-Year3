package automatedtesting.lab08.lab08.model;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

/**
 * A stored file owned by exactly one user. Bytes are kept in a BLOB so that
 * {@code sizeBytes} stays in sync with the content and the quota formula is testable.
 * {@code folderId == null} means the file lives at the storage root.
 */
@Entity
@Table(name = "files")
public class FileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long ownerId;

    private Long folderId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private long sizeBytes;

    @Lob
    @Column(nullable = false)
    private byte[] content;

    @Column(nullable = false)
    private Instant createdAt;

    protected FileEntity() {
    }

    public FileEntity(Long ownerId, Long folderId, String name, byte[] content) {
        this.ownerId = ownerId;
        this.folderId = folderId;
        this.name = name;
        this.content = content;
        this.sizeBytes = content.length;
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public Long getFolderId() {
        return folderId;
    }

    public void setFolderId(Long folderId) {
        this.folderId = folderId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getSizeBytes() {
        return sizeBytes;
    }

    public byte[] getContent() {
        return content;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
