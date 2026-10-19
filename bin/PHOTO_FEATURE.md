# Single profile picture extension

## Endpoints
- `POST /api/users/{id}/photo` multipart form field `file` (JPEG/PNG/WebP up to 5MB)
- `GET /api/users/{id}/photo` returns `{ "url": "<5-minute presigned S3 URL>" }`; 404 if absent
- `DELETE /api/users/{id}/photo` removes the photo

## Deployment
The existing Jenkinsfile provisions an S3 bucket and an EC2 instance profile via `infra/terraform/photos.tf`. Ansible writes `PHOTO_BUCKET_NAME` and `AWS_REGION` into the backend service environment. Hibernate `ddl-auto=update` adds the nullable `photo_key` column to the `USERS` table on startup. Jenkins needs AWS CLI for the bucket verification.

**Important security limitation:** The original application has no authentication/authorization and stores passwords in plaintext. As a result, photo endpoints are not access-controlled. This is suitable only for a restricted course demo; add Spring Security, password hashing, per-user authorization, and narrow CORS before exposing this application publicly. A signed URL is temporary but can be shared until expiry.

For production, use Flyway migrations instead of Hibernate schema update, disable `force_destroy`, and introduce transactional cleanup/retries for failed S3 deletion.

The pipeline's `backend_repo` and `frontend_repo` values must point to branches containing these updated files. Upload both modified folders to their corresponding Git repositories before running Jenkins; a local ZIP alone does not change GitHub. Existing `app_branch` is set in Ansible group vars. Terraform state should be persisted securely across Jenkins runs.
