package com.syn.usermanagement.service;

import com.syn.usermanagement.entity.User;
import com.syn.usermanagement.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import java.io.IOException;
import java.time.Duration;
import java.util.Set;
import java.util.UUID;

@Service
public class PhotoService {
    private final UserRepository users;
    private final S3Client s3;
    private final S3Presigner presigner;
    private final String bucket;
    private static final long MAX_SIZE = 5L * 1024 * 1024;
    private static final Set<String> TYPES = Set.of("image/jpeg", "image/png", "image/webp");

    public PhotoService(UserRepository users, S3Client s3, S3Presigner presigner,
                        @Value("${photo.bucket:}") String bucket) {
        this.users = users; this.s3 = s3; this.presigner = presigner; this.bucket = bucket;
    }
    private User user(Long id) {
        return users.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }
    private void configured() {
        if (bucket.isBlank()) throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Photo storage not configured");
    }
    public void upload(Long id, MultipartFile file) {
        configured();
        if (file.isEmpty() || file.getSize() > MAX_SIZE || !TYPES.contains(file.getContentType()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Upload a JPEG, PNG or WebP image up to 5 MB");
        User u = user(id);
        byte[] bytes;
        try { bytes = file.getBytes(); } catch (IOException ex) { throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot read image", ex); }
        // Verify magic bytes, not only client-supplied MIME type.
        boolean jpeg = bytes.length >= 3 && (bytes[0]&255)==0xff && (bytes[1]&255)==0xd8 && (bytes[2]&255)==0xff;
        boolean png = bytes.length >= 8 && (bytes[0]&255)==0x89 && bytes[1]==0x50 && bytes[2]==0x4e && bytes[3]==0x47 && bytes[4]==0x0d && bytes[5]==0x0a && bytes[6]==0x1a && bytes[7]==0x0a;
        boolean webp = bytes.length >= 12 && new String(bytes,0,4,java.nio.charset.StandardCharsets.US_ASCII).equals("RIFF") && new String(bytes,8,4,java.nio.charset.StandardCharsets.US_ASCII).equals("WEBP");
        if (!(jpeg && file.getContentType().equals("image/jpeg") || png && file.getContentType().equals("image/png") || webp && file.getContentType().equals("image/webp")))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Image content does not match its type");
        String ext = jpeg ? "jpg" : png ? "png" : "webp";
        String key = "profiles/" + id + "/" + UUID.randomUUID() + "." + ext;
        s3.putObject(PutObjectRequest.builder().bucket(bucket).key(key).contentType(file.getContentType()).build(), RequestBody.fromBytes(bytes));
        String old = u.getPhotoKey();
        try { u.setPhotoKey(key); users.save(u); }
        catch (RuntimeException ex) { s3.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(key).build()); throw ex; }
        if (old != null) s3.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(old).build());
    }
    public String url(Long id) {
        configured(); String key = user(id).getPhotoKey();
        if (key == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No photo");
        return presigner.presignGetObject(GetObjectPresignRequest.builder().signatureDuration(Duration.ofMinutes(5))
            .getObjectRequest(GetObjectRequest.builder().bucket(bucket).key(key).build()).build()).url().toString();
    }
    public void delete(Long id) {
        configured(); User u = user(id); String key = u.getPhotoKey();
        if (key == null) return;
        u.setPhotoKey(null); users.save(u);
        s3.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(key).build());
    }
}
